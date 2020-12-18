import React from 'react';
import { message } from 'antd';
import { ProFormSelect, QueryFilter, ProFormText, ProFormDatePicker } from '@ant-design/pro-form';

export default (props) => {
    let { onSearch } = props;

    return (
        <QueryFilter
            defaultCollapsed
            onFinish={async (values) => {
                onSearch(values);
                console.log(values);
            }}
        >
            <ProFormText name="num" label="仓库编号" placeholder="请输入仓库编号" />
            <ProFormText
                name="name"
                label="仓库名称"
                placeholder="请输入仓库名称"
            />
            <ProFormSelect
                options={[
                    {
                        value: '仓库部门',
                        label: '仓库部门',
                    },
                    {
                        value: '仓库管理部',
                        label: '仓库管理部',
                    },
                    {
                        value: '公司总部',
                        label: '公司总部',
                    },
                ]}
                name="department"
                label="所属部门"
            />
            <ProFormSelect
                options={[
                    {
                        value: '成品仓库',
                        label: '成品仓库',
                    },
                    {
                        value: '半成品仓库',
                        label: '半成品仓库',
                    },
                    {
                        value: '原料仓库',
                        label: '原料仓库',
                    },
                ]}
                name="type"
                label="仓库类型"
            />
        </QueryFilter>
    );
};