
import React from 'react';
import { Link } from 'react-router-dom';
import User from '../../service/user-service.jsx';
import Pagination from 'antd/lib/pagination';
import { Table, Divider, Button, Card, Tooltip, Input, Row, Col } from 'antd';
import LocalStorge from '../../util/LogcalStorge.jsx';
const localStorge = new LocalStorge();
const _user = new User();
const Search = Input.Search;

class UserList extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            list: [],
            pageNum: 1,
            perPage: 10,
            listType: 'list',
            searchKeyword: ''
        };
    }
    componentDidMount() {
        this.loadUserList();
    }
    loadUserList() {
        let listParam = {};
        listParam.pageNum = this.state.pageNum;
        listParam.perPage = this.state.perPage;
        // 如果是搜索的话，需要传入搜索类型和搜索关键字
        if (this.state.listType === 'search') {
            listParam.keyword = this.state.searchKeyword;
        }
        _user.getUserList(listParam).then(response => {
            this.setState(response.data);
        }, errMsg => {
            this.setState({
                list: []
            });
            // _mm.errorTips(errMsg);
        });
    }
    // 页数发生变化的时候
    onPageNumChange(pageNum) {
        this.setState({
            pageNum: pageNum
        }, () => {
            this.loadUserList();
        });
    }
    // 数据变化的时候
    onValueChange(e) {
        let name = e.target.name,
            value = e.target.value.trim();
        this.setState({
            [name]: value
        });
    }
    // 搜索
    onSearch(searchKeyword) {
        let listType = searchKeyword === '' ? 'list' : 'search';
        this.setState({
            listType: listType,
            pageNum: 1,
            searchKeyword: searchKeyword
        }, () => {
            this.loadUserList();
        });
    }
    deleteUser(id) {
        if (confirm('确认删除吗？')) {
            _user.delUser(id).then(response => {
                alert("删除成功");
                this.loadUserList();
            }, errMsg => {
                alert("删除失败");
                // _mm.errorTips(errMsg);
            });
        }
    }
    //展示当前行信息
    showCurRowMessage(record) {
        alert("key:" + record.userId + " name:" + record.userName + " description:" + record.description);
    }

    render() {
        this.state.list.map((item, index) => {
            item.key = index;
        })
        const userinfos = localStorge.getStorage('userInfo');
        const dataSource = this.state.list;
        let self = this;
        const columns = [{
            title: 'ID',
            dataIndex: 'userId',
            key: 'userId'
        }, {
            title: '姓名',
            dataIndex: 'userName',
            key: 'userName',
            render: function (text, record, index) {
                return <Link to={`/user/UserView/${record.id}`}>{text}</Link>;
            }
        }, {
            title: '描述',
            dataIndex: 'description',
            key: 'description',
            maxWidth: '200px'
        }, {
            title: '角色',
            dataIndex: 'isAdminText',
            key: 'isAdminText'
        }, {
            title: '入职时间',
            dataIndex: 'creationDate',
            key: 'creationDate'
        }, {
            title: '操作',
            dataIndex: '操作',
            render: (text, record) => (
                <span>
                    {record.userId != '1' ? <Link to={`/user/userInfo/${record.id}`}>编辑</Link> : ''}
                    <Divider type="vertical" />
                    {record.userId == '1' || record.id == userinfos.id ? '' : <a onClick={() => this.deleteUser(`${record.id}`)} href="javascript:;">删除</a>}
                </span>
            ),
        }];

        return (
            <div style={{ backgroundColor: '#e8e8e8' }}>
                <div  style={{height:'50px', backgroundColor: '#ffffff',fontSize:'18px',lineHeight:'50px', paddingLeft:'50px', fontFamily:'微软雅黑', color: '#000', fontWeight:'500'}}>用户管理</div>
                <div style={{padding:'20px'}}>               
                    <Card  bodyStyle={{ backgroundColor: 'ececec' }}>
                    <Row>
                        <Col xs={24} sm={12}>
                            <Search
                                style={{ maxWidth: 300, marginBottom: '10px' }}
                                placeholder="请输入..."
                                enterButton="查询"
                                onSearch={value => this.onSearch(value)}
                            />
                        </Col>
                        <Col xs={24} sm={12}>
                            <Button href="#/user/userInfo/null" style={{ float: "right", marginRight: "30px" }} >新建用户</Button>

                        </Col>
                    </Row>
                    <Table dataSource={dataSource} columns={columns} pagination={false} />
                    <Pagination current={this.state.pageNum}
                        total={this.state.total}
                        onChange={(pageNum) => this.onPageNumChange(pageNum)} />
                </Card>
                </div>

            </div>

        )
    }
}

export default UserList;