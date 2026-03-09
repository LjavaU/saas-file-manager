package com.example.saasfile.manager;

import com.example.saasfile.dto.fileUpload.*;

public interface FileUploadDirectManager {

    PresignedUploadResp createPresignedUploadUrl(PresignedUploadInitReq req);

    UploadCompleteResp uploadCallback(UploadCallbackReq req);

    MultipartUploadInitResp initMultipartUpload(MultipartUploadInitReq req);

    MultipartUploadSignResp signMultipartPart(MultipartUploadSignReq req);

    UploadCompleteResp completeMultipartUpload(MultipartUploadCompleteReq req);
}
