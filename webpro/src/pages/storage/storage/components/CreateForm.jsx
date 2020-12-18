import React, { useState, useEffect } from 'react';
import { Button, message, Form, Tree } from 'antd';
import ProForm, {
    ModalForm,
    ProFormText,
    ProFormDatePicker,
    ProFormSelect,
} from '@ant-design/pro-form';
import { PlusOutlined } from '@ant-design/icons';
import HttpService from '@/utils/HttpService.jsx'

export default ({ onFinish }) => {
    return (
        <ModalForm
            title="新增仓库"
            trigger={
                <Button type="primary">
                    <PlusOutlined />
        新增
          </Button>
            }
            onFinish={async values => {
                console.log('onFinish', values);
                let isOk = false;
                await HttpService.post('reportServer/storage/addStorage', JSON.stringify(values))
                    .then(res => {
                        if (res.resultCode == "1000") {
                            isOk = true;
                            message.success(res.message);
                            onFinish();
                        } else {
                            message.error(res.message);
                        }
                    });
                console.log('isOk', isOk);
                return isOk;
            }}
        >
            <ProForm.Group>

                <ProFormText
                    width="m"
                    name="num"
                    label="仓库编号"
                />
                <ProFormText
                    width="m"
                    name="name"
                    label="仓库名称"
                    placeholder="请输入仓库名称"
                />
            </ProForm.Group>
            <ProForm.Group>
                <ProFormSelect
                    width="m"
                    name="type"
                    label="仓库类型"
                    request={
                        async () => {

                            const result = await HttpService.post('reportServer/baseData/listBaseDataByType', JSON.stringify({
                                type: 'storage_type'
                            }));

                            if (result.resultCode == '1000') {
                                return Promise.resolve(result.data);
                            } else {
                                message.error('数据获取失败')
                                return Promise.resolve([]);
                            }
                        }
                    }
                />
                <ProFormText
                    width="m"
                    name="attribute_name"
                    label="作用"
                    placeholder="请输入仓库名称"
                />
            </ProForm.Group>

            <ProForm.Group>
                <ProFormSelect
                    width="m"
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
                <ProFormDatePicker width="m" name="time" label="租赁时间" />
            </ProForm.Group>
            <ProForm.Group>
                <ProFormText
                    width="m"
                    name="area"
                    label="面积"
                />
                <ProFormText
                    width="m"
                    name="address"
                    label="地址"
                />
            </ProForm.Group>
            <ProForm.Group>

                <ProFormText
                    width="m"
                    name="contacts_name"
                    label="联系人"
                />
                <ProFormText
                    width="m"
                    name="contacts_tel"
                    label="电话"
                />
            </ProForm.Group>

        </ModalForm>
    );
}
