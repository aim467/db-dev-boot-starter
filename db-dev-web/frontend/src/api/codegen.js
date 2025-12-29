import request from "./request";

/**
 * 生成代码
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
