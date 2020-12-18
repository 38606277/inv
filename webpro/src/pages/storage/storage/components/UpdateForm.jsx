import React, { useState, useEffect } from 'react';
import { Button, message, Form, Tree } from 'antd';
import ProForm, {
    ModalForm,
    ProFormText,
    ProFormDatePicker,
    ProFormSelect,
} from '@ant-design/pro-form';
import HttpService from '@/utils/HttpService.jsx'

export default ({ visible, initData, onFinish, onVisibleChange }) => {
    console.log('visible', visible)
    console.log('initData', initData)
    const [form] = Form.useForm();//获取form 在布局进行绑定
    form.setFieldsValue(initData);
    return (
        <ModalForm
            form={form}
            title="编辑仓库"
            visible={visible}
            onFinish={async values => {
                console.log('onFinish', values);
                values.id = initData.id;
                let isOk = false;
                await HttpService.post('reportServer/storage/updateStorage', JSON.stringify(values))
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
            onVisibleChange={onVisibleChange}

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
