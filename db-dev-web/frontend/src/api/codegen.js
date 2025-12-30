import request from "./request";

/**
 * 生成代码并获取下载链接
 */
export const generate = (config) => {
  return request.post("/codegen/generate", config);
};

/**
 * 预览代码
 */
export const preview = (config) => {
  return request.post("/codegen/preview", config);
};

/**
 * 下载生成的代码
 */
export const getDownloadUrl = (fileName) => {
  return `/api/codegen/download/${fileName}`;
};
