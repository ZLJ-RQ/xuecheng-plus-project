package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

import java.io.File;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName);

    MediaFiles addMediaFilesToDb(Long companyId, UploadFileParamsDto uploadFileParamsDto, String fileId, String bucket, String objectName);


    /**
     * @param fileMd5 文件的md5
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean>
     * @description 检查文件是否存在
     * @author 若倾
     * @date 2023/2/4 13:22
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * @param fileMd5    文件的md5
     * @param chunkIndex 分块序号
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     * @description 检查分块是否存在
     * @author 若倾
     * @date 2023/2/4 13:22
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * @param fileMd5 文件md5
     * @param chunk   分块序号
     * @param bytes   文件字节
     * @return com.xuecheng.base.model.RestResponse
     * @description 上传分块
     * @author 若倾
     * @date 2023/2/4 13:22
     */
    RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes);


    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5
     * @param chunkTotal          分块总和
     * @param uploadFileParamsDto 文件信息
     * @return com.xuecheng.base.model.RestResponse
     * @description 合并分块
     * @author 若倾
     * @date 2023/2/4 13:22
     */
    RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);


    /**
     * @param id 文件id
     * @return com.xuecheng.media.model.po.MediaFiles 文件信息
     * @description 根据id查询文件信息
     * @author 若倾
     * @date 2023/2/4 13:22
     */
    MediaFiles getFileById(String id);

    /**
     * @description 从minIO中下载文件
     * @param file
     * @param bucket
     * @param objectName
     * @return java.io.File
     * @author 若倾
     * @date 2023/2/6 20:45
    */
    File downloadFileFromMinIO(File file, String bucket, String objectName) ;
    /**
     * @description 根据md5值获取文件存储位置
     * @param fileMd5
     * @param fileExt
     * @return java.lang.String
     * @author 若倾
     * @date 2023/2/6 20:49
    */
     String getFilePathByMd5(String fileMd5, String fileExt) ;

     /***
      * @description 添加文件到MInIO
      * @param filePath
      * @param bucket
      * @param objectName
      * @return void
      * @author 若倾
      * @date 2023/2/6 20:55
     */
     void addMediaFilesToMinIO(String filePath, String bucket, String objectName);



}
