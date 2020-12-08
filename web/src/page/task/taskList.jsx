/*
* @Author: Rosen
* @Date:   2018-01-31 13:10:47
* @Last Modified by:   Rosen
* @Last Modified time: 2018-02-01 16:30:04
*/
import React        from 'react';
import { Link }     from 'react-router-dom';
import TaskService         from '../../service/task-service.jsx';
import {Pagination,Card,Table,Input,Tooltip}   from 'antd';
import  LocalStorge  from '../../util/LogcalStorge.jsx';
const localStorge = new LocalStorge();
const _product      = new TaskService();
const Search = Input.Search;
class TaskList extends React.Component{
    constructor(props){
        super(props);
        this.state = {
            list            : [],
            currentPage     : 1 ,
            perPage         : 10,
            listType:'list'
        };
    }
    
    componentDidMount(){
        this.loadProductList();
    }
    // 加载商品列表
    loadProductList(){
        let listParam = {};
        listParam.userId = localStorge.getStorage('userInfo').userId;
        listParam.currentPage  = this.state.currentPage;
        listParam.perPage  = this.state.perPage;
        // 如果是搜索的话，需要传入搜索类型和搜索关键字
        if(this.state.listType === 'search'){
            listParam.keyword    = this.state.searchKeyword;
        }
        // 请求接口
        _product.getTaskList(listParam).then(response => {
                this.setState(response.data);
        }, errMsg => {
            this.setState({
                list : []
            });
            localStorge.errorTips(errMsg);
        });
    }
    // 搜索
    onSearch(searchKeyword){
       
        this.setState({
            currentPage         : 1,
            searchKeyword   : searchKeyword
        }, () => {
            this.loadProductList();
        });
    }
    // 页数发生变化的时候
    onPageNumChange(currentPage){
        this.setState({
            currentPage : currentPage
        }, () => {
            this.loadProductList();
        });
    }
    
    render(){
        this.state.list.map((item,index)=>{
            item.key=index;
        })
        const dataSource = this.state.list;
          
          const columns = [{
            title: '填报名称',
            dataIndex: 'taskname',
            key: 'taskname',
            render: function(text, record, index) {
                return <Link to={ `/task/taskInfoView/${record.taskid}` }>{text}</Link>;
              } 
          }, {
            title: '填报开始时间',
            dataIndex: 'startdate',
            key: 'startdate',
          }, {
            title: '填报结束时间',
            dataIndex: 'enddate',
            key: 'enddate',
          }];
        return (
            <div id="page-wrapper">
                <Card title="已办任务列表" >
                        <Tooltip>
                            <Search
                                style={{ width: 300,marginBottom:'10px' }}
                                placeholder="请输入..."
                                enterButton="查询"
                                onSearch={value => this.onSearch(value)}
                                />
                        </Tooltip>   
                        <Table dataSource={dataSource} columns={columns} pagination={false} />
                        
                        <Pagination current={this.state.currentPage} 
                        total={this.state.total} 
                         onChange={(currentPage) => this.onPageNumChange(currentPage)}/>
                 </Card>
            </div>
        );
    }
}

export default TaskList;