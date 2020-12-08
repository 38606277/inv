
import React from 'react';
import { Link } from 'react-router-dom';
import Pagination from 'antd/lib/pagination';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import {
    Table,
    Divider,
    Button,
    Card,
    Tree,
    Input,
    Spin,
    Row,
    Col,
    Select,
    Radio,
    Modal,
} from 'antd';
import CubeService from '../../../service/CubeService.jsx';
import HttpService from '../../../util/HttpService.jsx';
const _cubeService = new CubeService();
import ReactEcharts from 'echarts-for-react';
const Search = Input.Search;
const FormItem = Form.Item;
const { TreeNode } = Tree;
const { Option } = Select;
import '../corp.css'






// const treeData = [
//     {
//         title: '人力资源',
//         key: '0-0',
//         children: [
//             {
//                 title: '0-0-0',
//                 key: '0-0-0',
//                 children: [
//                     { title: '0-0-0-0', key: '0-0-0-0' },
//                     { title: '0-0-0-1', key: '0-0-0-1' },
//                     { title: '0-0-0-2', key: '0-0-0-2' },
//                 ],
//             },
//             {
//                 title: '0-0-1',
//                 key: '0-0-1',
//                 children: [
//                     { title: '0-0-1-0', key: '0-0-1-0' },
//                     { title: '0-0-1-1', key: '0-0-1-1' },
//                     { title: '0-0-1-2', key: '0-0-1-2' },
//                 ],
//             },
//             {
//                 title: '0-0-2',
//                 key: '0-0-2',
//             },
//         ],
//     },
//     {
//         title: '财务',
//         key: '0-1',
//         children: [
//             { title: '0-1-0-0', key: '0-1-0-0' },
//             { title: '0-1-0-1', key: '0-1-0-1' },
//             { title: '0-1-0-2', key: '0-1-0-2' },
//         ],
//     },
//     {
//         title: '投资',
//         key: '0-2',
//     },
// ];

function onChange(value) {
    console.log(`selected ${value}`);
}

function onBlur() {
    console.log('blur');
}

function onFocus() {
    console.log('focus');
}

function onSearch(val) {
    console.log('search:', val);
}

export default class index extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            list: [],
            chartRow: [],
            pageNum: 1,
            perPage: 10,
            listType: 'list',
            cube_name: '',
            loading: false,
            treeData: [],
            buttontype: ['primary', 'default', 'default', 'default'],
            iView: 'list'

        };
    }
    componentDidMount() {
        this.loadCubeList();
        this.loadDataList();
    }

    getBarChart() {
        //  alert('aa');
        const option = {
            tooltip: {
                trigger: 'axis',
                axisPointer: { // 坐标轴指示器，坐标轴触发有效
                    type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: [{
                type: 'category',
                data: ['2010', '2011', '2012', '2013', '2014', '2015', '2016', '2016', '2018', '2019'],
                // this.state.list.map(function (item) {
                //     return item.year;
                // }),
                axisLine: {
                    lineStyle: {
                        color: '#8FA3B7',//y轴颜色
                    }
                },
                axisLabel: {
                    show: true,
                    textStyle: {
                        color: '#6D6D6D',
                    }
                },
                axisTick: { show: false }
            }],
            yAxis: [{
                type: 'value',
                splitLine: { show: false },
                //max: 700,
                splitNumber: 3,
                axisTick: { show: false },
                axisLine: {
                    lineStyle: {
                        color: '#8FA3B7',//y轴颜色
                    }
                },
                axisLabel: {
                    show: true,
                    textStyle: {
                        color: '#6D6D6D',
                    }
                },
            }],
            series: [

                {
                    name: '',
                    type: 'bar',
                    barWidth: '40%',
                    itemStyle: {
                        normal: {
                            color: '#4DB3F5'
                        }
                    },
                    stack: '信息',
                    data: this.state.chartRow
                    //  this.state.list.map(function (item) {
                    //     return item.amount;
                    // })

                    // [320, 132, 101, 134, 90, 30]
                }
            ]
        };
        return option;
    }

    showModal = (record) => {
        //将record转换为值
        let aRow = [];
        for (var key in record) {
            if ((key != 'index_name') || (key != 'unit') || (key != 'key'))
                aRow.push(record[key]); //获取对应的value值
        }

        this.setState({
            visible: true,
            chartTitle: record.index_name,
            chartRow: aRow
        });
    };

    getLineChart() {
        const option = {
            tooltip: {
                trigger: 'axis',
                axisPointer: { // 坐标轴指示器，坐标轴触发有效
                    type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
                }
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: [{
                type: 'category',
                data: ['2014', '2015', '2016', '2017', '2018', '2019'],
                axisLine: {
                    lineStyle: {
                        color: '#8FA3B7',//y轴颜色
                    }
                },
                axisLabel: {
                    show: true,
                    textStyle: {
                        color: '#6D6D6D',
                    }
                },
                axisTick: { show: false }
            }],
            yAxis: [{
                type: 'value',
                splitLine: { show: false },
                //max: 700,
                splitNumber: 3,
                axisTick: { show: false },
                axisLine: {
                    lineStyle: {
                        color: '#8FA3B7',//y轴颜色
                    }
                },
                axisLabel: {
                    show: true,
                    textStyle: {
                        color: '#6D6D6D',
                    }
                },
            }],
            series: [

                {
                    name: 'a',
                    type: 'line',
                    barWidth: '40%',
                    itemStyle: {
                        normal: {
                            color: '#4DB3F5'
                        }
                    },
                    stack: '信息',
                    data: [320, 132, 101, 134, 90, 30]
                }
            ]
        };
        return option;
    }


    loadCubeList() {
        let param = {
            FLEX_VALUE_SET_ID: 6
        };

        HttpService.post('/reportServer/FlexValue/getFlexValuesTree', JSON.stringify(param)).then(res => {
            if (res.resultCode == "1000") {
                this.setState({
                    treeData: res.data,
                });
            }
            else {
                message.error(res.message);
            }
        }, errMsg => {
            this.setState({
                list: [], loading: false
            });
        });
    }
    loadDataList() {
        let param = {
            index_catalog_id: 2
        };

        HttpService.post('/reportServer/index/getIndexValue', JSON.stringify(param)).then(res => {
            if (res.resultCode == "1000") {
                this.setState({
                    list: res.data,
                });
            }
            else {
                message.error(res.message);
            }
        }, errMsg => {
            this.setState({
                list: [], loading: false
            });
        });
    }
    // 页数发生变化的时候
    onPageNumChange(pageNum) {
        this.setState({
            pageNum: pageNum
        }, () => {
            this.loadCubeList();
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
    onSearch(cube_name) {
        let listType = cube_name === '' ? 'list' : 'search';
        this.setState({
            listType: listType,
            pageNum: 1,
            cube_name: cube_name
        }, () => {
            this.loadCubeList();
        });
    }
    deleteCube(id) {
        if (confirm('确认删除吗？')) {
            _cubeService.delCube(id).then(response => {
                alert("删除成功");
                this.loadCubeList();
            }, errMsg => {
                alert("删除失败");
                // _mm.errorTips(errMsg);
            });
        }
    }

    onExpand = expandedKeys => {
        console.log('onExpand', expandedKeys);
        // if not set autoExpandParent to false, if children expanded, parent can not collapse.
        // or, you can remove all expanded children keys.
        this.setState({
            expandedKeys,
            autoExpandParent: false,
        });
    };

    onCheck = checkedKeys => {
        console.log('onCheck', checkedKeys);
        this.setState({ checkedKeys });
    };

    //选择一个指标
    onSelect = (selectedKeys, info) => {
        //数据源
        console.log('selectedKeys', selectedKeys);
        console.log('info', info.node.props.dataRef.id);

        let param = { index_catalog_id: info.node.props.dataRef.id };
        let url = "/reportServer/index/getIndexValueWithColumn";
        HttpService.post(url, JSON.stringify(param)).then(res => {
            if (res.resultCode == "1000") {
                this.setState({
                    list: res.data,
                });
            }
            else {
                message.error(res.message);
            }
        }, errMsg => {
            this.setState({
                list: [], loading: false
            });
        });


    };

    onViewClick = (viewID, buttontype) => {

        let param = {
            FLEX_VALUE_SET_ID: viewID
        };
        if (buttontype == 3) {
            //数据源
            let param = {};
            let url = "reportServer/DBConnection/ListAll";
            HttpService.post(url, param).then(response => {
                this.setState({ treeData: response });
                // alert(JSON.stringify(this.state.treeData));
                // 设置高亮
                this.activeButton(buttontype);
            }, errMsg => {
                this.setState({
                    list: []
                });
            });

        } else {
            HttpService.post('/reportServer/FlexValue/getFlexValuesTree', JSON.stringify(param)).then(res => {
                if (res.resultCode == "1000") {
                    this.setState({
                        treeData: res.data,
                    });
                    // 设置高亮
                    this.activeButton(buttontype);

                }
                else {
                    message.error(res.message);
                }
            }, errMsg => {
                this.setState({
                    list: [], loading: false
                });
            });

        }


    }

    activeButton = i => {

        let aButtonType = [];
        for (var j = 0; j < this.state.buttontype.length; j++) {
            if (i == j) {
                aButtonType[j] = 'primary';

            } else {
                aButtonType[j] = 'default';
            }
        }
        this.setState({ buttontype: aButtonType });
    }
    handleOk = e => {
        console.log(e);
        this.setState({
            visible: false,
        });
    };

    handleCancel = e => {
        console.log(e);
        this.setState({
            visible: false,
        });
    };



    columns = [

        {
            title: '指标',
            dataIndex: 'index_name',
            width: 150,
            fixed: 'left',
            render: (text, record) => (
                <span>
                    <a onClick={() => this.showModal(record)} href="javascript:;">{text}({record.unit})</a>
                </span>
            ),
        },
        {
            title: '2010年',
            dataIndex: '2010',
        },
        {
            title: '2011年',
            dataIndex: '2012',
        },
        {
            title: '2013年',
            dataIndex: '2013',
        },
        {
            title: '2014年',
            dataIndex: '2014',
        },
        {
            title: '2015年',
            dataIndex: '2015',
        },
        {
            title: '2016年',
            dataIndex: '2016',
        },
        {
            title: '2017年',
            dataIndex: '2017',
        },
        {
            title: '2018年',
            dataIndex: '2018',
        },
        {
            title: '2019年',
            dataIndex: '2019',
        }
    ];


    renderTreeNodes = data =>
        data.map(item => {
            if (item.children) {
                return (
                    <TreeNode title={item.value} key={item.key} dataRef={item}>
                        {this.renderTreeNodes(item.children)}
                    </TreeNode>
                );
            }
            return <TreeNode title={item.value} key={item.key} dataRef={item} />;
        });

    renderView = () => {
        if (this.state.iView == 'list')
            return (<Table dataSource={this.state.list} columns={this.columns} pagination={false} scroll={{ x: 1300 }} />);
        else if (this.state.iView == 'column')
            return (
                <ReactEcharts
                    option={this.getBarChart()}
                    notMerge={true}
                    lazyUpdate={true}
                    style={{ width: '100%', height: '250px' }} />
            )
        else if (this.state.iView == 'line')
            return (
                <ReactEcharts
                    option={this.getLineChart()}
                    notMerge={true}
                    lazyUpdate={true}
                    style={{ width: '100%', height: '250px' }} />
            )
        else if (this.state.iView == 'column+list')
            return (
                <div>
                    <ReactEcharts
                        option={this.getBarChart()}
                        notMerge={true}
                        lazyUpdate={true}
                        style={{ width: '100%', height: '250px' }} />
                    <Table dataSource={this.state.list} columns={this.columns} pagination={false} />
                </div>
            )

        // if (this.state.iView == 'list')
        // {
        //     alert(this.state.iView);
        //     return
        //        <div>dsf</div>
        //     //  <Table dataSource={this.state.list} columns={this.columns} pagination={false} />
        // }

        // }else   if (this.state.iView == 'column')
        //     return <div><ReactEcharts
        //         option={getBarChart()}
        //         notMerge={true}
        //         lazyUpdate={true}
        //         style={{ width: '100%', height: '250px' }}
        //     /><div>dfdfdf</div></div>
        // else if (this.state.iView == 'line')
        //     return <ReactEcharts
        //         option={getLineChart()}
        //         notMerge={true}
        //         lazyUpdate={true}
        //         style={{ width: '100%', height: '250px' }}
        //     />


    };

    render() {
        this.state.list.map((item, index) => {
            item.key = index;
        })



        return (
            <div id="page-wrapper">
                <Spin spinning={this.state.loading} delay={100}>

                    <Card bodyStyle={{ backgroundColor: '#ececec', padding: '15px' }} >
                        <Card style={{ boxShadow: '0 2px 3px 0 rgba(0,0,0,.2)' }}>
                            <img style={{ height: '16px', width: '16px', marginRight: '10px' }} src={require('../../../asset/data-icon.png')} />
                            <label style={{ fontFamily: 'Roboto,San Francisco', fontSize: '16px', height: '22px', lineHeight: '22px', color: 'black' }}>统计指标分析</label>

                            <Divider dashed style={{ marginTop: '12px' }} />
                            <Row>
                                <Col sm={4}>
                                    <Tree
                                        onSelect={this.onSelect}

                                    >
                                        {this.renderTreeNodes(this.state.treeData)}
                                    </Tree>


                                </Col>
                                <Col sm={20}>
                                    <Card bodyStyle={{ padding: "8px", backgroundColor: '#fafafa' }}>
                                        <Radio.Group defaultValue="list" buttonStyle="solid" onChange={(e) => { this.setState({ iView: e.target.value }) }}>
                                            <Radio.Button value="list">列表</Radio.Button>
                                            <Radio.Button value="column">柱图</Radio.Button>
                                            <Radio.Button value="line">线图</Radio.Button>
                                            <Radio.Button value="column+list">柱图+列表</Radio.Button>
                                        </Radio.Group>

                                    </Card>
                                    {
                                        this.renderView()
                                    }
                                </Col>
                            </Row>
                        </Card>
                    </Card>
                </Spin>
                <Modal
                    title={this.state.chartTitle}
                    width='900px'
                    visible={this.state.visible}
                    onOk={this.handleOk}
                    onCancel={this.handleCancel}
                >
                    <ReactEcharts
                        option={this.getBarChart()}
                        notMerge={true}
                        lazyUpdate={true}
                        style={{ width: '100%', height: '350px' }} />
                </Modal>
            </div>
        )
    }
}
