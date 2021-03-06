import React from 'react'
import { Link }             from 'react-router-dom';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Table, Divider, Tag, Input, Select, Button, Card, Checkbox } from 'antd';

import HttpService from '../../util/HttpService.jsx';
import './function.scss';
const Option = Select.Option;



class EditOut extends React.Component {

  constructor(props) {
    super(props);
    this.props.onRef(this)
    this.state = { 
      data:[],
      formData:{},
      dictData:[],
      authData:[],
    };
  }

  componentDidMount() {

  }
  setFormValue(d) {

    //let arr=d.slice(0);
    this.setState({ data: d });
    let f = {};
    for (var i = 0; i < this.state.data.length; i++) {
      let rowObject = this.state.data[i];
      let keys = Object.getOwnPropertyNames(rowObject);
      for (var field of keys) {
        let fieldName = i + '-' + field;
        f[fieldName] = this.state.data[i][field];
      }
    }
    console.log(f);
    this.setState({ formData: f });
    this.props.form.setFieldsValue(this.state.formData);
    //this.props.form.setFieldsValue(this.state.formData);
  }

  getFormValue() {
    return this.state.data;
  }


  changeEvent(e) {
    // record.age=e.target.value; 
    console.log(e.target.id, e.target.value);
    let id = e.target.id;
    let index = id.split('-')[0];
    let field = id.split('-')[1]
    this.state.data[index][field] = e.target.value;
  }

  buttonClick() {
    console.log(this.props.form.getFieldsValue());
    console.log(this.state.data);
  }
  changeColumn() {
    this.refs.table.columns = this.columns1;



    this.arr.push(<Input />);
  }



  columns = [{
    title: '列ID',
    dataIndex: 'out_id',
    key: 'out_id',
    width: '120px',
    className: 'headerRow',
  }, {
    title: '列名',
    dataIndex: 'out_name',
    key: 'out_name',
    className: 'headerRow',
    render: (text, record, index) => {
      return (
        <Form>
          <Form.Item style={{ margin: 0 }}>
            {this.props.form.getFieldDecorator(index + '-' + 'out_name', {
              rules: [{
                required: true,
                message: `参数名是必须的！`,
              }]

            })(<Input onChange={e => this.changeEvent(e)} />)}
          </Form.Item>
        </Form>
      );
    }
  }, {
    title: '数据类型',
    dataIndex: 'datatype',
    key: 'datatype',
    className: 'headerRow',
    render: (text, record, index) => {
      return (
        <Form>

          <Form.Item style={{ margin: 0 }}>
            {this.props.form.getFieldDecorator(index + '-' + 'datatype', {
              rules: [{
                required: true,
                message: `数据类型是必须的！`,
              }],
            })(
              <Select style={{ minWidth: '80px' }} >
                <Option value="string">字符串</Option>
                <Option value="number">数字</Option>
                <Option value="date">日期</Option>
              </Select>
            )}
          </Form.Item>
        </Form>
      );
    }
  }, {
    title: '超链接',
    dataIndex: 'datatype',
    key: 'datatype',
    className: 'headerRow',
    render: function(text, record, index) {
      return <Link to={ `/user/UserView/${record.id}` }>配置</Link>;
    } 
  }
  ];



  render() {

    return (
      // <Button onClick={() => this.buttonClick()} >显示结果</Button>
      // <Button onClick={() => this.changeColumn()} >字段变更</Button>
      <Table ref="table" columns={this.columns} dataSource={this.state.data} size="small" bordered pagination={false} />
    )
  }
}

export default EditOut = Form.create()(EditOut);