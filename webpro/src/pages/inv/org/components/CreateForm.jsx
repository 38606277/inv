import React, { useState, useEffect } from 'react';
import { Button, message, Form, Tree } from 'antd';
import ProForm, {
  ModalForm,
  ProFormText,
  ProFormSelect,
} from '@ant-design/pro-form';

import OrgTree from './OrgTree.jsx'

//import HttpService from '@/utils/HttpService.jsx'

export default ({ orgPid, modalVisible, onCancel, onSubmit }) => {
  const [form] = Form.useForm();//获取form 在布局进行绑定
  form.resetFields();
  form.setFieldsValue({ org_pid: orgPid })
  return (
    <ModalForm
      form={form}
      title="新增仓库"
      visible={modalVisible}
      onFinish={(values) => onSubmit(values)}
      destroyOnClose
      modalProps={{
        onCancel: () => onCancel(),
      }}
    >

      <Form.Item
        name="org_pid"
        label="上级节点"
        rules={[{ required: true }]}
      >
        <OrgTree
          onChange={(value) => {
            form.setFieldsValue('org_pid', value)
            console.log('OrgTree', value)
          }} />
      </Form.Item>

      <ProForm.Group>
        <ProFormText
          width="m"
          name="org_name"
          label="名称"
          rules={[{ required: true }]}
        />
        <ProFormSelect
          width="m"
          name="org_type"
          label="类型"
          rules={[{ required: true }]}
          options={[
            {
              value: '1',
              label: '仓库',
            },
            {
              value: '2',
              label: '门店',
            }
          ]}
        />
      </ProForm.Group>

      <ProForm.Group>
        <ProFormText
          width="m"
          name="address"
          label="地址"
        />
        <ProFormText
          width="m"
          name="contacts"
          label="联系人"
        />
      </ProForm.Group>
    </ModalForm>
  );
}
