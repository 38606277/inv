
import React from 'react';
import { Link } from 'react-router-dom';
import DB from '../../../service/DbService.jsx';
import { DatabaseOutlined } from '@ant-design/icons';
import { Table, Divider, Button, Card, Input } from 'antd';
// import { SearchOutlined } from '@ant-design/icons';
const db = new DB();

class DbsList extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            list: []
        };
    }
    componentDidMount() {
        this.loadDbList();
    }
    loadDbList() {
        let listParam = {};
        db.getDbList(listParam).then(response => {
            this.setState({ list: response });
        }, errMsg => {
            this.setState({
                list: []
            });
        });
    }

    deleteDb(name) {
        if (confirm('确认删除吗？')) {
            db.deleteDb(name).then(response => {
                alert("删除成功");
                this.loadDbList();
            }, errMsg => {
                alert("删除失败");
            });
        }
    }


    render() {
        this.state.list.map((item, index) => {
            item.key = index;
        })
        const dataSource = this.state.list;
        let self = this;
        const columns = [{
            title: '数据源名称',
            dataIndex: 'name',
            key: 'name',
            render: function (text, record, index) {
                return <Link to={`/dbs/dbView/${record.name}`}>{text}</Link>;
            }
        }, {
            title: '数据源类型',
            dataIndex: 'dbtype',
            key: 'dbtype'
        }, {
            title: '操作',
            dataIndex: '操作',
            render: (text, record) => (
                <span>
                    <Link to={`/dbs/dbInfo/${record.name}`}>编辑</Link><Divider type="vertical" />
                    {record.name != 'system' && record.name != 'form' ?
                        <a onClick={() => this.deleteDb(`${record.name}`)} href="javascript:;">删除</a> : "系统数据库"}
                </span>
            ),
        }];

        return (
            <div id="page-wrapper">
                <Card title="创建数据源">
                    <Button href="#/dbs/dbInfo/null" type="primary" icon={<DatabaseOutlined />}>新建数据源</Button>
                    <Input.Search
                        style={{ maxWidth: 300, marginBottom: '10px', float: "right" }}
                        placeholder="请输入..."
                        enterButton="查询"
                    />
                    <Table dataSource={dataSource} columns={columns} pagination={false} style={{ marginTop: '10px' }} />

                </Card>

            </div>
        );
    }
}

export default DbsList;