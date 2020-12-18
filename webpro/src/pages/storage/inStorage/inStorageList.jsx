import React, { useRef } from 'react';
import { Button, Space, Modal, message } from 'antd';
import { EllipsisOutlined, QuestionCircleOutlined, SearchOutlined, } from '@ant-design/icons';
import ProTable, { TableDropdown } from '@ant-design/pro-table';

import CreateFrom from './components/CreateFrom.jsx';

import '@ant-design/pro-form/dist/form.css';
import '@ant-design/pro-table/dist/table.css';
import '@ant-design/pro-layout/dist/layout.css';

import HttpService from '@/utils/HttpService.jsx';

const { confirm } = Modal;

//定义列
const columns = [
    {
        title: '订单编号',
        dataIndex: 'order_num',
        valueType: 'text',
    },
    {
        title: '商品名称',
        dataIndex: 'product_name',
        valueType: 'text',
    },
    {
        title: '商品编号',
        dataIndex: 'product_code',
        valueType: 'text',
    },
    {
        title: '货号',
        dataIndex: 'atr_num',
        valueType: 'text',
    },
    {
        title: '批次',
        dataIndex: 'batch',
        valueType: 'text',
    },
    {
        title: '大小',
        dataIndex: 'size',
        valueType: 'text',
    },
    {
        title: '颜色',
        dataIndex: 'color',
        valueType: 'text',
    },
    {
        title: '订单数量',
        dataIndex: 'num',
        valueType: 'text',
    },
    {
        title: '入库数量',
        dataIndex: 'in_num',
        valueType: 'text',
    },
    {
        title: '仓库',
        dataIndex: 'storage_location_name',
        valueType: 'text',
    },
    {
        title: '库位编号',
        dataIndex: 'storage_location_code',
        valueType: 'text',
    },
    {
        title: '入库类型',
        dataIndex: 'type',
        valueType: 'text',
    },
    {
        title: '入库人',
        dataIndex: 'in_storage_user_name',
        valueType: 'text',
    }, {
        title: '供应商',
        dataIndex: 'supplier',
        valueType: 'text',
    },
    {
        title: '制单人',
        dataIndex: 'create_user_name',
        valueType: 'text',
    },
    {
        title: '制单时间',
        dataIndex: 'create_time',
        valueType: 'text',
    },
    {
        title: '状态',
        dataIndex: 'status',
        valueType: 'text',
    },
    {
        title: '审核人',
        dataIndex: 'approval_user_name',
        valueType: 'text',
    },
    {
        title: '审核时间',
        dataIndex: 'approval_time',
        valueType: 'text',
    },
    {
        title: '操作',
        width: 180,
        key: 'option',
        valueType: 'option',
        render: (text, record) => {
            if (record.status == 1) {
                return [
                    <a key="link3" >查看</a>,
                    <a key="link4" >打印</a>,
                    <a key="link4" >删除</a>,
                ]
            } else {
                return [
                    <a key="link1" >查看</a>,
                    <a key="link2" >审核</a>,
                    <a key="link3" >查看</a>,
                    <a key="link4" >打印</a>,
                    <a key="link4" >删除</a>,
                ]
            }


        },
    },
];
//删除按钮事件
const onDeleteClickListener = (ref, selectedRows) => {

    if (selectedRows.length < 1) {
        message.error('请选择需要删除的内容');
        return;
    }
    console.log('onDeleteClickListener', selectedRows);

    confirm({
        title: '温馨提示',
        content: `您确定要删除及全部下级分类吗？`,
        okText: '确定',
        cancelText: '取消',
        okType: 'danger',
        onOk() {
            deleteByIds(ref, selectedRows);
        },
        onCancel() {

        },
    });

}
//删除
const deleteByIds = (ref, selectedRows) => {
    HttpService.post('reportServer/assetCategoryExtension/deleteByIds', JSON.stringify({ dataList: selectedRows }))
        .then(res => {
            if (res.resultCode == "1000") {
                //刷新
                // 清空选中项
                ref.current.clearSelected();
                ref.current.reload();

            } else {
                message.error(res.message);
            }
        });
}

//获取数据
const fetchData = async (params, sort, filter) => {
    console.log('getByKeyword', params, sort, filter);
    // current: 1, pageSize: 20
    let requestParam = {
        pageNum: params.current,
        perPage: params.pageSize
    }
    const result = await HttpService.post('reportServer/storage/listStorageByPage', JSON.stringify(requestParam));
    console.log('result : ', result);
    return Promise.resolve({
        data: result.data.list,
        total: result.data.total,
        success: result.resultCode == "1000"
    });
}

const InStorageList = () => {
    console.log('绘制布局')
    const ref = useRef();
    return (
        <ProTable
            actionRef={ref}
            columns={columns}
            request={fetchData}
            rowKey="id"
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
                    <a onClick={() => onDeleteClickListener(ref, selectedRows)}> 批量删除</a>
                </Space>
            )}
            pagination={{
                showQuickJumper: true,
            }}
            search={{
                layout: 'vertical',
                defaultCollapsed: false,
            }}
            dateFormatter="string"
            toolBarRender={() => [
                <CreateFrom onFinish={() => {
                    ref.current.reload();
                }} />
            ]}
        />
    );
}

export default InStorageList;