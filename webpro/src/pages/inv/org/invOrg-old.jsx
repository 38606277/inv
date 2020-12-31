import React, { useState, useEffect } from 'react';
import { PlusOutlined, DeleteOutlined, EditOutlined } from '@ant-design/icons';
import {
    Input,
    Form,
    Button,
    Card,
    Row,
    Col,
    Tree,
    message,
    Table,
    Divider,
    Modal,
    TreeSelect,
} from 'antd';
import HttpService from '@/utils/HttpService.jsx';
import "./invOrg.css"
const { TreeNode } = Tree;
const { Column, ColumnGroup } = Table;
const Search = Input.Search;
const { confirm } = Modal;

//类别新增对话框
const CollectionCreateForm = (props) => {
    const { visible, onActionCallBack, edit, treeData, initData } = props;
    const [form] = Form.useForm();//获取form 在布局进行绑定
    form.resetFields();
    form.setFieldsValue(initData);

    const onSelectChange = value => {
        console.log('CollectionCreateForm onSelectChange  value: ', form, value);
        form.setFieldsValue({ org_pid: value == undefined ? '0' : value });
    }

    const parseJson = (treeData) => {
        let i = 0;
        for (i in treeData) {
            let item = treeData[i];
            item["title"] = item.name;
            item["value"] = item.code;
            parseJson(item.children);
        }
        return treeData;
    }


    const formItemLayout = {
        labelCol: {
            xs: { span: 24 },
            sm: { span: 8 },
        },
        wrapperCol: {
            xs: { span: 24 },
            sm: { span: 12 },
        },
    };


    let mTreeData = [{
        title: '无上级（一级）',
        value: '0'
    }]
    if (typeof treeData != 'undefined') {
        mTreeData = mTreeData.concat(parseJson(treeData));
    }

    //点击确定
    const onOkListener = () => {
        //edit ? onEdit : onCreate
        console.log('okListener')
        form.submit();
    }

    const onCancelListener = () => {
        onActionCallBack('cancel', {});
    }


    const onFinish = (values) => {
        console.log('onFinish:', values);
        onActionCallBack(edit ? 'edit' : 'create', values);
    };

    const onFinishFailed = (errorInfo) => {
        console.log('Failed:', errorInfo);
    };


    return (
        <Modal
            visible={visible}
            title={edit ? '编辑分类' : '新增分类'}
            okText="保存"
            cancelText="取消"
            onCancel={onCancelListener}
            onOk={onOkListener}
        >

            <Form
                form={form}
                name="categoryDialog"
                onFinish={onFinish}
                onFinishFailed={onFinishFailed}
            >

                <Form.Item style={{ display: "none" }} label="" name="id"
                    rules={[{ required: false, message: '' }]}
                >
                    <Input type='text' name='id' />
                </Form.Item>

                <Form.Item {...formItemLayout} label="" style={{ display: "none" }}
                    name="old_code"
                    rules={[{ required: false, message: '' }]}
                >
                    <Input type='text' name='old_code' />
                </Form.Item>

                <Form.Item {...formItemLayout} label="分类编号"
                    name="code"
                    rules={[{ required: true, message: '请输入分类编号' }]}
                >
                    <Input type='text' name='code' />
                </Form.Item>
                <Form.Item {...formItemLayout} label="分类名称" name="name"
                    rules={[{ required: true, message: '请输入分类名称' }]}>
                    <Input type='text' name='name' />
                </Form.Item>

                <Form.Item {...formItemLayout} label="上级分类" name="org_pid"
                    rules={[{ required: true, message: '请选择上级分类' }]}
                >
                    <TreeSelect
                        allowClear="true"
                        style={{ width: '100%' }}
                        dropdownStyle={{ maxHeight: 260, overflow: 'auto' }}
                        treeData={mTreeData}
                        placeholder="请选择上级分类"
                        treeDefaultExpandAll
                        onChange={onSelectChange}
                    />
                </Form.Item>
                <Form.Item {...formItemLayout} label="预计使用期限"
                    name="life"
                    rules={[{ required: false, message: '请输入预计使用期限' }]}>
                    <Input type='text' name='life' />
                </Form.Item>
                <Form.Item {...formItemLayout} label="单位"
                    name="unit"
                    rules={[{ required: false, message: '请输入单位' }]}>
                    <Input type='text' name='unit' />
                </Form.Item>

            </Form>
        </Modal>
    );

}

const InvOrgOld = () => {
    //初始化state
    const [visible, setVisible] = useState(false);
    const [treeData, setTreeData] = useState([]);
    const [dataList, setDataList] = useState([]);
    const [initData, setInitData] = useState({});
    const [edit, setEdit] = useState(false);

    //对应 componentDidMount 
    useEffect(() => {
        refreshData();
    }, [])


    const refreshData = () => {
        setVisible(false);
        getAllChildrenRecursionById('0');
        getByKeyword('');
    }

    //定义方法 ------------

    const getAllChildrenRecursionById = (orgPid) => {
        HttpService.post('reportServer/invOrg/getAllChildrenRecursionById', JSON.stringify({ org_pid: orgPid }))
            .then(res => {
                if (res.resultCode == "1000") {
                    setTreeData(res.data)
                }
                else {
                    message.error(res.message);
                }
            });
    }


    const onTreeSelect = (selectedKeys, info) => {

        if (info.selected) {
            let param = {
                org_id: selectedKeys[0]
            }
            HttpService.post('reportServer/invOrg/getAllChildrenListById', JSON.stringify(param))
                .then(res => {
                    if (res.resultCode == "1000") {
                        setDataList(res.data);
                    }
                    else {
                        message.error(res.message);
                    }
                });
        }
    }


    const getByKeyword = (keyword) => {

        let param = {
            keyword: keyword
        }

        HttpService.post('reportServer/invOrg/getByKeyword', JSON.stringify(param))
            .then(res => {
                if (res.resultCode == "1000") {
                    setDataList(res.data);
                }
                else {
                    message.error(res.message);
                }
            });
    }

    const onDeleteClickListener = (category) => {
        confirm({
            title: '温馨提示',
            content: `您确定要删除${category.name}及全部下级分类吗？`,
            okText: '确定',
            cancelText: '取消',
            okType: 'danger',
            onOk() {
                deleteById(category);
            },
            onCancel() {

            },
        });

    }

    const deleteById = (category) => {
        HttpService.post('reportServer/invOrg/deleteById', JSON.stringify(category))
            .then(res => {
                if (res.resultCode == "1000") {
                    refreshData();
                } else {
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
                            {item.org_name}
                            <PlusOutlined class="element" style={{ fontSize: '14px', color: '#1890ff' }} onClick={() => {
                                onAddClickListener(item.org_id);
                            }} />
                            <EditOutlined class="element" style={{ fontSize: '14px', color: '#1890ff' }} onClick={() => {
                                onEditClickListener(item);
                            }} />
                            <DeleteOutlined class="element" style={{ fontSize: '14px', color: '#1890ff' }} onClick={() => {
                                onDeleteClickListener(item);
                            }} />
                        </div>
                    } key={item.org_id} dataRef={item}>
                        {renderTreeNodes(item.children)}
                    </TreeNode>
                );
            } else {
                return (
                    <TreeNode style={{ width: "100%" }} key={item.org_id} title={<div class="father" style={{ width: "100%" }}>
                        {item.org_name}
                        <PlusOutlined class="element" style={{ fontSize: '14px', color: '#1890ff' }} onClick={() => {
                            onAddClickListener(item.org_id);
                        }} />
                        <EditOutlined class="element" style={{ fontSize: '14px', color: '#1890ff' }} onClick={() => {
                            onEditClickListener(item);
                        }} />
                        <DeleteOutlined class="element" style={{ fontSize: '14px', color: '#1890ff' }} onClick={() => {
                            onDeleteClickListener(item);
                        }} />

                    </div>} isLeaf={true} dataRef={item} />
                );
            }
        });

    }

    const onAddClickListener = (orgPid) => {
        console.log(orgPid)
        setInitData({ org_pid: orgPid })
        setVisible(true);
        setEdit(false);

    };

    const handleCreate = (values) => {

        HttpService.post('reportServer/invOrg/add', JSON.stringify(values))
            .then(res => {
                if (res.resultCode == "1000") {

                    refreshData();
                } else {
                    message.error(res.message);
                }
            });
    };

    const onEditClickListener = (category) => {
        category.old_org_id = category.org_id;
        setInitData(category)
        setVisible(true)
        setEdit(true);
    }

    const handleEdit = (values) => {

        HttpService.post('reportServer/invOrg/updateById', JSON.stringify(values))
            .then(res => {
                if (res.resultCode == "1000") {
                    refreshData();
                } else {
                    message.error(res.message);
                }
            });

    }

    const onActionCallBack = (action, values) => {
        console.log('onActionCallBack : ', action)
        if (action == 'create') {
            handleCreate(values)
        } else if (action == 'edit') {
            handleEdit(values)
        } else {
            setVisible(false)
        }
    }
    const onImportClickListener = () => {

    }

    return (
        <div id="page-wrapper">

            <Card title="仓库管理" style={{ height: '100%' }}>

                <div>
                    <Button type="primary" style={{ marginLeft: '10px' }} onClick={() => onAddClickListener('0')}>新增分类</Button>
                    <Button type="primary" style={{ marginLeft: '10px' }} onClick={() => onImportClickListener()}>批量导入</Button>

                    <Search
                        placeholder="搜索名称、地址、联系人"
                        onSearch={value => getByKeyword(value)}
                        style={{ width: '200px', float: 'right' }}
                    />
                </div>


                <Row style={{ marginTop: '16px' }}>
                    <Col xs={24} sm={6}>
                        <Tree
                            defaultExpandAll={true}
                            autoExpandParent={true}
                            style={{ width: "100%" }}
                            showLine={true}
                            onSelect={onTreeSelect}
                        >
                            {renderTreeNodes(treeData)}
                        </Tree>
                    </Col>


                    <Col xs={24} sm={18}>

                        <Table bordered dataSource={dataList} rowKey={"org_id"} pagination={false} >
                            <Column
                                title="编号"
                                dataIndex="org_id"
                            />
                            <Column
                                title="名称"
                                dataIndex="org_name"
                            />
                            <Column
                                title="地址"
                                dataIndex="address"
                            />
                            <Column
                                title="联系人"
                                dataIndex="contacts"
                            />
                            <Column
                                title="操作"
                                render={(text, record) => (
                                    <div > <span style={{ color: "#1890ff" }} onClick={() => {
                                        onEditClickListener(record);
                                    }}>
                                        编辑
                                    </span>
                                        <Divider type="vertical"></Divider>
                                        <span style={{ color: "#1890ff" }} onClick={() => {
                                            onDeleteClickListener(record);
                                        }}>
                                            删除
                                    </span>
                                    </div>
                                )}
                            />
                        </Table>
                    </Col>
                </Row>

            </Card>

            <CollectionCreateForm
                visible={visible}
                onActionCallBack={onActionCallBack}
                treeData={treeData}
                edit={edit}
                initData={initData}
            />

        </div>
    )

}


export default InvOrgOld;




