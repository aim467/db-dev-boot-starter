<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>数据库表结构文档</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 40px; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        h1 { color: #333; border-bottom: 3px solid #409eff; padding-bottom: 10px; }
        h2 { color: #409eff; margin-top: 30px; }
        h3 { color: #606266; }
        .info { background: #ecf5ff; padding: 15px; border-radius: 4px; margin: 20px 0; color: #409eff; }
        table { width: 100%; border-collapse: collapse; margin: 15px 0; }
        th, td { border: 1px solid #dcdfe6; padding: 12px; text-align: left; }
        th { background: #409eff; color: white; }
        tr:nth-child(even) { background: #fafafa; }
        tr:hover { background: #ecf5ff; }
        .primary-key { background: #fdf6ec; }
        .index { background: #f0f9eb; padding: 10px; margin: 10px 0; border-radius: 4px; }
        .table-section { margin: 30px 0; padding: 20px; border: 1px solid #e4e7ed; border-radius: 8px; }
        .table-name { color: #409eff; font-size: 18px; margin-bottom: 10px; }
        .remark { color: #909399; font-style: italic; }
    </style>
</head>
<body>
    <div class="container">
        <h1>数据库表结构文档</h1>
        <div class="info">
            <p><strong>数据库:</strong> ${metadata.productName} ${metadata.productVersion}</p>
            <p><strong>生成时间:</strong> ${generateTime}</p>
            <p><strong>表数量:</strong> ${metadata.tables?size}</p>
        </div>

        <#list metadata.tables as table>
        <div class="table-section">
            <h2 class="table-name">${table.tableName}<#if table.remarks?has_content> <span class="remark">- ${table.remarks}</span></#if></h2>

            <h3>字段列表</h3>
            <table>
                <thead>
                    <tr>
                        <th>字段名</th>
                        <th>类型</th>
                        <th>可空</th>
                        <th>默认值</th>
                        <th>自增</th>
                        <th>备注</th>
                    </tr>
                </thead>
                <tbody>
                    <#list table.columns as col>
                    <tr<#if col.primaryKey> class="primary-key"</#if>>
                        <td>${col.columnName}<#if col.primaryKey> 🔑</#if></td>
                        <td>${col.formattedType}</td>
                        <td>${col.nullable?string('是', '否')}</td>
                        <td>${col.defaultValue!'-'}</td>
                        <td>${col.autoIncrement?string('是', '否')}</td>
                        <td>${col.remarks!'-'}</td>
                    </tr>
                    </#list>
                </tbody>
            </table>

            <#if table.primaryKeys?has_content>
            <p><strong>主键:</strong> ${table.primaryKeys?join(', ')}</p>
            </#if>

            <#if table.indexes?has_content>
            <h3>索引</h3>
            <div class="index">
                <#assign indexGroups = {} />
                <#list table.indexes as idx>
                    <#assign key = idx.indexName />
                    <#if !indexGroups[key]??>
                        <#assign indexGroups = indexGroups + {key: []} />
                    </#if>
                    <#assign indexGroups = indexGroups + {key: indexGroups[key] + [idx]} />
                </#list>
                <#list indexGroups?keys as indexName>
                    <#assign indexes = indexGroups[indexName] />
                    <#assign cols = [] />
                    <#list indexes as idx>
                        <#if idx.columnName?has_content>
                            <#assign cols = cols + [idx.columnName] />
                        </#if>
                    </#list>
                    <p>• <strong>${indexName}</strong>: ${cols?join(', ')}<#if indexes[0].unique> (唯一)</#if></p>
                </#list>
            </div>
            </#if>
        </div>
        </#list>
    </div>
</body>
</html>