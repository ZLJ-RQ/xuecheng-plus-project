package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Value("${minio.bucket.videofiles}")
    private String bucket_videoFiles;

    @Autowired
    MediaFileService currentProxy;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(queryMediaParamsDto.getFilename()), MediaFiles::getFilename, queryMediaParamsDto.getFilename());
        queryWrapper.eq(StringUtils.isNotEmpty(queryMediaParamsDto.getFileType()), MediaFiles::getFileType, queryMediaParamsDto.getFileType());
        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {

        String fileMd5 = DigestUtils.md5Hex(bytes);

        if (StringUtils.isEmpty(folder)) {
            folder = getFileFolder(true, true, true);
        } else if (folder.indexOf("/") < 0) {
            folder = folder + "/";
        }

        String filename = uploadFileParamsDto.getFilename();
        if (objectName == null) {
            //用md5值来代替objectName
            //后缀名也得补上
            objectName = fileMd5 + filename.substring(filename.lastIndexOf("."));
        }
        objectName = folder + objectName;

        try {
            //上传到minio
            addMediaFilesToMinIO(bytes, bucket_files, objectName);
            //上传到数据库
            MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, uploadFileParamsDto, fileMd5, bucket_files, objectName);
            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
            return uploadFileResultDto;
        } catch (Exception e) {
            XueChengPlusException.cast("上传过程中出错");
        }
        return null;
    }

    public void addMediaFilesToMinIO(String filePath, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder().bucket(bucket).object(objectName).filename(filePath).build();
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("文件上传成功:{}", filePath);
        } catch (Exception e) {
            XueChengPlusException.cast("文件上传到文件系统失败");
        }
    }

    /**
     * @param bytes      字节数组
     * @param bucket     桶
     * @param objectName 桶下的具体文件目录
     * @return void
     * @description 将文件上传到minIO
     * @author 若倾
     * @date 2023/2/3 14:59
     */
    private void addMediaFilesToMinIO(byte[] bytes, String bucket, String objectName) {
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//默认是未知二进制流
        //查找content-type,用文件扩展名去匹配类型
        //首先保证有扩展名
        if (objectName.indexOf(".") > 0) {
            String extension = objectName.substring(objectName.lastIndexOf("."));
            contentType=getMineTypeByExtension(extension);
        }
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    //InputStream stream 流, long objectSize 对象大小, long partSize 分片大小 -1为最小分片5M,最大分片5T,最多10000个分片
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(contentType)
                    .build();
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            XueChengPlusException.cast("上传文件到文件系统出错");
        }
    }

    private String getMineTypeByExtension(String extension){
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//默认是未知二进制流
        //查找content-type,用文件扩展名去匹配类型
        //首先保证有扩展名
        if (StringUtils.isNotEmpty(extension)) {
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            //然后保证匹配到的扩展名存在
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }
        }
        return contentType;
    }

    @Transactional
    @Override
    public MediaFiles addMediaFilesToDb(Long companyId, UploadFileParamsDto uploadFileParamsDto, String fileId, String bucket, String objectName) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileId);
            mediaFiles.setFileId(fileId);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setFilePath(objectName);
            String extension=null;
            String filename = uploadFileParamsDto.getFilename();
            if (StringUtils.isNotEmpty(filename)&&filename.indexOf(".")>0){
                extension=filename.substring(filename.lastIndexOf("."));
            }
            String mineType=getMineTypeByExtension(extension);
            if (mineType.contains("image")||mineType.contains("mp4")){
                mediaFiles.setUrl("/" + bucket + "/" + objectName);
            }
            mediaFiles.setBucket(bucket);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                XueChengPlusException.cast("保存文件信息失败");
            }
            if (mineType.equals("video/x-msvideo")){
                MediaProcess mediaProcess = new MediaProcess();
                BeanUtils.copyProperties(mediaFiles,mediaProcess);
                mediaProcess.setStatus("1");//未处理
                mediaProcessMapper.insert(mediaProcess);
            }
        }
        return mediaFiles;
    }

    //检查文件是否存在,1.数据库文件表要存在2.分布式文件系统要存在
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            return RestResponse.success(false);
        }
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(mediaFiles.getBucket()).object(mediaFiles.getFilePath()).build();
        try {
            InputStream inputStream = minioClient.getObject(getObjectArgs);
            //可能不报异常,但输入流也需要判断是否存在着
            if (inputStream == null) {
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            //文件不存在
            return RestResponse.success(false);
        }
        //文件存在
        return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket_videoFiles).object(chunkFilePath).build();
        try {
            InputStream inputStream = minioClient.getObject(getObjectArgs);
            //可能不报异常,但输入流也需要判断是否存在着
            if (inputStream == null) {
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            //分块文件不存在
            return RestResponse.success(false);
        }
        //分块文件存在
        return RestResponse.success(true);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes) {
        //得到分块文件的目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunk;
        try {
            addMediaFilesToMinIO(bytes, bucket_videoFiles, chunkFilePath);
            return RestResponse.success(true);
        } catch (Exception e) {
            log.debug("上传分块文件:{},失败:{}", chunkFilePath, e.getMessage());
        }
        return RestResponse.validfail(false, "上传分块失败");
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //先下载分块
        File[] files = checkChunkStatus(fileMd5, chunkTotal);


        //得到合并后文件的扩展名
        String filename = uploadFileParamsDto.getFilename();
        //扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        File tempMergeFile = null;
        try {
            try {
                //创建一个临时文件作为合并文件
                tempMergeFile = File.createTempFile("merge", extension);
            } catch (IOException e) {
                XueChengPlusException.cast("创建临时合并文件出错");
            }

            //再合并分块成一个完整的文件
            try (
                    //创建合并文件的流对象
                    RandomAccessFile raf_write = new RandomAccessFile(tempMergeFile, "rw");) {
                //创建一个缓冲区
                byte[] b = new byte[1024];
                for (File file : files) {
                    //创建读取分块文件的流对象
                    try (RandomAccessFile raf_read = new RandomAccessFile(file, "r");) {

                        int len = -1;
                        //将数据读取到缓冲区,读到最后会显示-1
                        while ((len = raf_read.read(b)) != -1) {
                            //再从缓冲区中拿数据出来
                            raf_write.write(b, 0, len);
                        }
                    }
                }
            } catch (IOException e) {
                XueChengPlusException.cast("合并文件过程出错");
            }
            //到这已经合并完成
            //通过md5值来校验合并后的文件是否正确
            //获取合并文件的流对象,来得到它的md5值
            try {
                FileInputStream fileInputStream = new FileInputStream(tempMergeFile);
                String mergeMd5Hex = DigestUtils.md5Hex(fileInputStream);
                if (!fileMd5.equals(mergeMd5Hex)) {
                    log.debug("合并文件校验不通过,文件路径:{},原始文件md5:{}", tempMergeFile.getAbsolutePath(), fileMd5);
                    XueChengPlusException.cast("合并文件校验不通过");
                }
            } catch (IOException e) {
                log.debug("合并文件校验出错,文件路径:{},原始文件md5:{}", tempMergeFile.getAbsolutePath(), fileMd5);
                XueChengPlusException.cast("合并文件校验出错");
            }

//  addMediaFilesToMinIO(byte[] bytes);不能用这个,这个传的是字节数组,文件几百兆,字节数组也几百兆,大文件不合适
            //拿到合并文件在minio的存储路径
            String mergeFilePath = getFilePathByMd5(fileMd5, extension);
            //将合并后的文件上传到文件系统
            addMediaFilesToMinIO(tempMergeFile.getAbsolutePath(), bucket_videoFiles, mergeFilePath);
            //将文件信息入库保存
            uploadFileParamsDto.setFileSize(tempMergeFile.length());
            addMediaFilesToDb(companyId, uploadFileParamsDto, fileMd5, bucket_videoFiles, mergeFilePath);
            return RestResponse.success(true);
        } finally {
            //删除临时分块文件
            if (files != null) {
                for (File chunkFile : files) {
                    if (chunkFile.exists()) {
                        chunkFile.delete();
                    }
                }
            }
            //删除合并的临时文件
            if (tempMergeFile != null) {
                tempMergeFile.delete();
            }
        }
    }

    @Override
    public MediaFiles getFileById(String id) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(id);
        if (mediaFiles==null){
            XueChengPlusException.cast("文件不存在");
        }
        String url = mediaFiles.getUrl();
        if (StringUtils.isEmpty(url)){
            XueChengPlusException.cast("文件尚未处理,请稍后预览");
        }
        return mediaFiles;
    }

    //获取合并文件的路径
    public String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }


    //检查所有分块是否上传完毕
    private File[] checkChunkStatus(String fileMd5, int chunkTotal) {
        //得到分块文件的目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //分块文件数组
        File[] files = new File[chunkTotal];
        //开始下载
        for (int i = 0; i < chunkTotal; i++) {
            //分块文件的路径
            String chunkFilePath = chunkFileFolderPath + i;
            //分块文件
            File chunkFile = null;
            try {
                //分块文件需要下载在服务端本地,所以创建临时文件,前缀会自动加uuid
                chunkFile = File.createTempFile("chunk", null);
            } catch (IOException e) {
                e.printStackTrace();
                XueChengPlusException.cast("下载分块时创建临时文件出错");
            }
            //下载分块文件到本地
            chunkFile = downloadFileFromMinIO(chunkFile, bucket_videoFiles, chunkFilePath);
            //添加分块文件到分块文件数组
            files[i] = chunkFile;
        }
        return files;
    }

    //根据桶和文件路径从minio下载文件
    public File downloadFileFromMinIO(File file, String bucket, String objectName) {
        //获取文件的位置
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(objectName).build();
        try (
                //得到文件的输入流
                InputStream inputStream = minioClient.getObject(getObjectArgs);
                //得到文件的输出流
                FileOutputStream outputStream = new FileOutputStream(file);) {
            //将文件输入到指定的输出文件
            IOUtils.copy(inputStream, outputStream);
            //返回文件
            return file;
        } catch (Exception e) {
            XueChengPlusException.cast("查询分块文件失败");
        }
        return null;
    }


    //根据md5值获取分块文件夹目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    //根据日期生成目录
    private String getFileFolder(boolean year, boolean month, boolean day) {
        //生成一个日期格式转化对象
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(new Date());
        //将日期分片
        String[] dateStringArray = dateString.split("-");
        //生成一个存储组装好目录的字符串
        StringBuffer folderString = new StringBuffer();
        //判断传进来的参数是否为true,若为true生成对应的日期目录
        if (year) {
            folderString.append(dateStringArray[0]).append("/");
        }
        if (month) {
            folderString.append(dateStringArray[1]).append("/");
        }
        if (day) {
            folderString.append(dateStringArray[2]).append("/");
        }
        return folderString.toString();
    }
}
