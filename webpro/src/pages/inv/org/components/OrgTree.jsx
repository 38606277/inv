import React, { useState, useEffect } from 'react';
import { TreeSelect } from 'antd';
import HttpService from '@/utils/HttpService.jsx'

const OrgTree = (props) => {
    const { value, onChange } = props;
    const [treeData, setTreeData] = useState([]);

    useEffect(() => {
        refreshData();
    }, [])

    const refreshData = () => {
        HttpService.post('reportServer/invOrg/getAllChildrenRecursionById', JSON.stringify({ org_pid: '0' }))
            .then(res => {
                if (res.resultCode == "1000") {
                    setTreeData(res.data)
                } else {
                    message.error(res.message);
                }
            });
    }
    console.log('OrgTree', props)

    return (<TreeSelect
        style={{ width: '100%' }}
        dropdownStyle={{ overflow: 'auto' }}
        treeData={treeData}
        placeholder="请选择"
        treeDefaultExpandAll
        allowClear
        onChange={onChange}
        defaultValue={value}
    />);
}

export default OrgTree;