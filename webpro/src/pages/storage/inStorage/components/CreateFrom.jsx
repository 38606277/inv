import React, { useState, useEffect } from 'react';
import { Button, message, Form, Tree } from 'antd';
import ProForm, {
    ModalForm,
    ProFormText,
    ProFormDateRangePicker,
    ProFormSelect,
} from '@ant-design/pro-form';
import { PlusOutlined } from '@ant-design/icons';
import HttpService from '@/utils/HttpService.jsx'

const { TreeNode } = Tree;


export default ({ onFinish }) => {

    const [treeData, setTreeData] = useState([]);
    const [checkedKeys, setCheckedKeys] = useState([]);
    const [form] = Form.useForm();//获取form 在布局进行绑定

    //对应 componentDidMount 
    useEffect(() => {
        getAllChildrenRecursionByCode();
    }, [])

    const getAllChildrenRecursionByCode = () => {
        HttpService.post('reportServer/assetCategory/getAllChildrenRecursionByCode', JSON.stringify({ parent_code: '0' }))
            .then(res => {
                if (res.resultCode == "1000") {
                    setTreeData([
                        {
                            "path": "",
                            "unit": "",
                            "code": 0,
                            "level": 1,
                            "children": res.data,
                            "name": "所有资产分类（全局）",
                            "parent_code": "-1",
                            "id": 0,
                            "life": ""
                        }
                    ])
                }
                else {
                    message.error(res.message);
                }
            });
    }
    const renderTreeNodes = data => {
        return data.map(item => {
            if (item.children) {
                return (
                    <TreeNode style={{ width: "100%" }} title={
                        <div class="father" style={{ width: "100%" }}>
                            {item.name}
                        </div>
                    } key={item.code} dataRef={item}>
                        {renderTreeNodes(item.children)}
                    </TreeNode>
                );
            }
            return (<TreeNode style={{ width: "100%" }} key={item.code} title={<div class="father" style={{ width: "100%" }}>
                {item.name}
            </div>} isLeaf={true} dataRef={item} />);
        });
    }

    const checkTree = (rule, value, callback) => {
        const { getFieldValue } = form;
        if (getFieldValue('parent_code_list') && 0 < getFieldValue('parent_code_list').length) {
            callback()
        } else {
            callback('请选择应用范围(分类)')
        }
    }

    const onCheck = checkedKeys => {
        console.log('onCheck', checkedKeys);
        form.setFieldsValue({
            parent_code_list: checkedKeys
        });
        setCheckedKeys(checkedKeys)
    };


    return (
        <ModalForm
            form={form}
            title="新建表单"
            trigger={
                <Button type="primary">
                    <PlusOutlined />
        新增
          </Button>
            }
            onFinish={async values => {
                console.log('onFinish', values);
                let isOk = false;
                await HttpService.post('reportServer/assetCategoryExtension/add', JSON.stringify(values))
                    .then(res => {
                        if (res.resultCode == "1000") {
                            isOk = true;
                            message.success('提交成功！');
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
                    name="attribute_name"
                    label="扩展信息名称"
                    tooltip="最长为 24 位"
                    placeholder="请输入扩展信息名称"
                />
                <ProFormSelect
                    width="m"
                    options={[
                        {
                            value: '1',
                            label: '是',
                        },
                        {
                            value: '0',
                            label: '否',
                        },
                    ]}
                    name="is_required"
                    label="是否必填"
                />
            </ProForm.Group>
            <ProForm.Group>
                <Form.Item width="m" label="应用范围(分类)" name="parent_code_list"
                    rules={[{ type: 'array', required: true, message: '请选择应用范围(分类)', validator: checkTree }]}>
                    <Tree
                        style={{ height: '260px', borderStyle: 'solid', overflow: 'auto' }}
                        checkable
                        defaultExpandAll={true}
                        autoExpandParent={true}
                        onCheck={onCheck}
                        checkedKeys={checkedKeys}
                    >
                        {renderTreeNodes(treeData)}
                    </Tree>
                </Form.Item>
            </ProForm.Group>
        </ModalForm>
    );
}
