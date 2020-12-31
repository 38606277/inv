import React, { useState, useEffect, useRef } from 'react';
import { Button, Space, message, Tree, Row, Col, Modal, TreeSelect } from 'antd';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable from '@ant-design/pro-table';
import HttpService from '@/utils/HttpService.jsx';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
const { TreeNode } = Tree;
const { confirm } = Modal;


import CreateForm from './components/CreateForm.jsx';
import UpdateForm from './components/UpdateForm.jsx';

//获取数据
const fetchData = async (params, sort, filter) => {
    console.log('getByKeyword', params, sort, filter);
    // current: 1, pageSize: 20
    let requestParam = {
        pageNum: params.current,
        perPage: params.pageSize,
        ...params
    }

    const result = await HttpService.post('reportServer/invOrg/getByKeyword', JSON.stringify(requestParam));
    console.log('result : ', result);
    return Promise.resolve({
        data: result.data,
        total: result.data.length,
        success: result.resultCode == "1000"
    });
}

const InvOrg = () => {
    const ref = useRef();
    const [treeData, setTreeData] = useState([]);
    const [path, setPath] = useState('-1-');
    const [createModalVisible, handleCreateModalVisible] = useState(false);
    const [updateModalVisible, handleUpdateModalVisible] = useState(false);

    const [initData, setInitData] = useState({});//用于编辑赋初始值
    const [orgPid, setOrgPid] = useState();//用于编辑赋初始值

    useEffect(() => {
        refreshData();
    }, [])

    const columns = [
        // {
        //     title: '仓库编号',
        //     dataIndex: 'org_id',
        //     valueType: 'text',
        //     hideInSearch: true,
        // },
        {
            title: '名称',
            dataIndex: 'org_name',
            key: 'org_name',
            valueType: 'text',
        },
        {
            title: '类型',
            dataIndex: 'org_type',
            key: 'org_type',
            valueType: 'text',
            valueEnum: {
                1: '仓库',
                2: '门店'
            }
        },
        {
            title: '地址',
            dataIndex: 'address',
            key: 'address',
            valueType: 'text',
        },

        {
            title: '联系人',
            dataIndex: 'contacts',
            key: 'contacts',
            valueType: 'text',
        },
        {
            title: '操作',
            width: 180,
            valueType: 'option',
            render: (text, record) => [
                <a onClick={() => {
                    setInitData(record);
                    handleUpdateModalVisible(true);
                }}>编辑</a>,
                <a onClick={() => {
                    onDeleteClickListener([record.org_id]);
                }}>删除</a>,
            ]
        },
    ];

    const refreshData = () => {
        getAllChildrenRecursionById('0');
    }


    const getAllChildrenRecursionById = (orgPid) => {
        HttpService.post('reportServer/invOrg/getAllChildrenRecursionById', JSON.stringify({ org_pid: orgPid }))
            .then(res => {
                if (res.resultCode == "1000") {
                    setTreeData(res.data)
                } else {
                    message.error(res.message);
                }
            });
    }

    const onTreeSelect = (item) => {
        if (path != item.item) {
            setPath(item.path)
        }
    }

    //删除按钮事件
    const onDeleteClickListener = (selectedRowKeys) => {

        if (selectedRowKeys.length < 1) {
            message.error('请选择需要删除的内容');
            return;
        }
        confirm({
            title: '温馨提示',
            content: `您确定要删除吗？`,
            okText: '确定',
            cancelText: '取消',
            okType: 'danger',
            onOk() {
                deleteByIds(selectedRowKeys);
            },
            onCancel() {

            },
        });
    }

    //删除
    const deleteByIds = (selectedRowKeys) => {
        if (selectedRowKeys.length < 1) {
            message.error('请选择需要删除的内容');
            return;
        }
        HttpService.post('reportServer/invOrg/deleteByIds', JSON.stringify({ ids: selectedRowKeys.toString() }))
            .then(res => {
                if (res.resultCode == "1000") {
                    //刷新
                    // 清空选中项
                    if (ref.current) {
                        ref.current.clearSelected();
                        ref.current.reload();
                    }
                    refreshData();
                } else {
                    message.error(res.message);
                }
            });
    }

    return (
        <PageContainer>

            <Row style={{ marginTop: '16px' }}>
                <Col xs={24} sm={6}>
                    <Tree
                        defaultExpandAll
                        style={{ width: "100%", minHeight: "450px", padding: "24px" }}
                        showLine

                        treeData={treeData}
                        titleRender={(item) => {
                            return (<div style={{ width: "100%" }} key={item.org_id}>
                                <span onClick={() => {
                                    onTreeSelect(item);
                                }}>{item.org_name}</span>
                                <PlusOutlined style={{ marginLeft: '10px', fontSize: '14px', color: '#1890ff' }} onClick={() => {
                                    handleCreateModalVisible(true);
                                    setOrgPid(item.org_id)
                                }} />
                                <EditOutlined style={{ marginLeft: '10px', fontSize: '14px', color: '#1890ff' }} onClick={() => {
                                    setInitData(item)
                                    handleUpdateModalVisible(true);
                                }} />
                                <DeleteOutlined style={{ marginLeft: '10px', fontSize: '14px', color: '#1890ff' }} onClick={() => {
                                    onDeleteClickListener([item.org_id]);
                                }} />
                            </div>)
                        }}
                    >
                    </Tree>
                </Col>
                <Col xs={24} sm={1}>
                </Col>
                <Col xs={24} sm={17}>

                    <ProTable
                        headerTitle="仓库列表"
                        actionRef={ref}
                        columns={columns}
                        request={fetchData}
                        rowKey="org_id"
                        params={{ path: path }}
                        rowSelection={{
                            // 自定义选择项参考: https://ant.design/components/table-cn/#components-table-demo-row-selection-custom
                            // 注释该行则默认不显示下拉选项
                            //selections: [Table.SELECTION_ALL, Table.SELECTION_INVERT],
                        }}
                        tableAlertRender={({ selectedRowKeys, selectedRows, onCleanSelected }) => (
                            <Space size={24}>
                                <span>
                                    已选 {selectedRowKeys.length} 项
                    <a
                                        style={{
                                            marginLeft: 8,
                                        }}
                                        onClick={onCleanSelected}
                                    >
                                        取消选择
                    </a>
                                </span>
                            </Space>
                        )}
                        tableAlertOptionRender={({ selectedRowKeys, selectedRows, onCleanSelected }) => (
                            <Space size={16}>
                                <a onClick={() => onDeleteClickListener(selectedRowKeys)}> 批量删除</a>
                            </Space>
                        )}
                        pagination={{
                            showQuickJumper: true,
                        }}
                        search={{
                            defaultCollapsed: true
                        }}
                        dateFormatter="string"
                        toolBarRender={() => [
                            <Button key='create' type="primary" onClick={() => {
                                setOrgPid(1)
                                handleCreateModalVisible(true);
                            }}>
                                <PlusOutlined />
                                    新增
                          </Button>


                        ]}
                    />
                </Col>
            </Row>

            <CreateForm
                orgPid={orgPid}
                onCancel={() => handleCreateModalVisible(false)}
                modalVisible={createModalVisible}
                onSubmit={async (value) => {
                    console.log('CreateForm', value);
                    let res = await HttpService.post('reportServer/invOrg/add', JSON.stringify(value));


                    if (res.resultCode == "1000") {
                        handleCreateModalVisible(false);
                        if (ref.current) {
                            ref.current.clearSelected();
                            ref.current.reload();
                        }
                        refreshData();
                    } else {
                        message.error(res.message);
                    }

                }} />


            <UpdateForm
                initData={initData}
                onCancel={() => handleUpdateModalVisible(false)}
                modalVisible={updateModalVisible}
                onSubmit={async (value) => {
                    console.log('UpdateForm', value);
                    let res = await HttpService.post('reportServer/invOrg/updateById', JSON.stringify(value));
                    if (res.resultCode == "1000") {
                        handleUpdateModalVisible(false);
                        if (ref.current) {
                            ref.current.clearSelected();
                            ref.current.reload();
                        }
                        refreshData();
                    }
                }} />

        </PageContainer >
    )

}
export default InvOrg;




