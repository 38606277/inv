import React from 'react';
import { Form, Icon as LegacyIcon } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Input, Select, Button, Card, Row, Col, message, Upload, Divider } from 'antd';
const FormItem = Form.Item;
const Option = Select.Option;

import HttpService from '@/utils/HttpService.jsx'


class menuEdit extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      action: this.props.match.params.action,
      id: this.props.match.params.id,
      isReadOnly: false,
      valid: '1',
      func_pid: '0',
      rootMenuList: []
    };


  }

  //初始化加载调用方法
  componentDidMount() {
    this.loadRootMenuList();

    if (this.state.action == 'update') {
      HttpService.post("reportServer/menu/getMenuInfo", JSON.stringify({ func_id: this.state.id }))
        .then(res => {
          if (res.resultCode == "1000") {
            this.props.form.setFieldsValue(res.data);
            this.setState({
              valid: res.data.valid,
              func_pid: res.data.func_pid
            })
          }
          else
            message.error(res.message);
        });
    }

  }

  loadRootMenuList() {
    HttpService.post("reportServer/menu/getRootMenuList", JSON.stringify({}))
      .then(res => {
        if (res.resultCode == "1000") {
          this.props.form.setFieldsValue(res.data);
          this.setState({
            rootMenuList: res.data
          })
        }
        else
          message.error(res.message);
      });
  }


  //提交
  onSaveClick(closed) {
    let formInfo = this.props.form.getFieldsValue();

    this.props.form.validateFieldsAndScroll((err, values) => {
      if (!err) {

        console.log("提交数据", formInfo);
        formInfo.valid = this.state.valid
        formInfo.func_pid = this.state.func_pid
        if (this.state.action == 'create') {
          HttpService.post("reportServer/menu/createMenu", JSON.stringify(formInfo))
            .then(res => {
              if (res.resultCode == "1000") {
                message.success('创建成功！id号：' + res.data);
                this.setState({ action: 'update' });
                this.props.form.setFieldsValue({ asset_id: res.data });
              }
              else
                message.error(res.message);

            });

        } else if (this.state.action == 'update') {
          formInfo.func_id = this.state.id;
          HttpService.post("reportServer/menu/updateMenu", JSON.stringify(formInfo))
            .then(res => {
              if (res.resultCode == "1000") {
                message.success(`保存成功！`)
              }
              else
                message.error(res.message);
            });
        }
        if (closed) {
          window.location.href = "/menu/menuManager";
        }
      }
    });
  }


  //编辑字段对应值
  onSelectChange(name, value) {
    this.setState({ [name]: value });
    this.props.form.setFieldsValue({ [name]: value });
  }


  render() {

    let { rootMenuList } = this.state;
    const { getFieldDecorator } = this.props.form;
    const formItemLayout = {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 12 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 12 },
      },
    };
    const formItemLayout1 = {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 4 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 20 },
      },
    };

    const uploadButton = (
      <div>
        <LegacyIcon type={this.state.loading ? 'loading' : 'plus'} />
        <div className="ant-upload-text">Upload</div>
      </div>
    );
    const { imageUrl } = this.state;

    let rootMenuItems = [];
    rootMenuItems.push(<Option value='0' >根菜单</Option>)
    for (let i in rootMenuList) {
      let rootMenu = rootMenuList[i];
      rootMenuItems.push(<Option value={`${rootMenu.func_id}`} >{rootMenu.func_name}</Option>)
    }


    return (
      <div id="page-wrapper">
        <Card title={<b> {this.state.isReadOnly ? '菜单详情' : this.state.action == 'update' ? '编辑菜单' : '新建菜单'}</b>}
          extra={<span>
            <Button style={{ marginLeft: '10px' }} onClick={() => this.onSaveClick(true)} disabled={this.state.isReadOnly}>保存并关闭</Button>
            <Button style={{ marginLeft: '10px' }} onClick={() => this.onSaveClick(false)} disabled={this.state.isReadOnly}>保存</Button>
            <Button href="#/menu/menuManager" style={{ marginLeft: '10px' }}>返回</Button>
          </span>} >
          <Form >
            <FormItem style={{ display: 'none' }}>
              {getFieldDecorator('func_id')(
                <Input type='text' readOnly={this.state.isReadOnly} />
              )}
            </FormItem>
            <Row>
              <Col xs={24} sm={8}>
                <FormItem {...formItemLayout} label="菜单名称">
                  {getFieldDecorator('func_name', {
                    rules: [{ required: true, message: '请输入菜单名称!' }],
                  })(
                    <Input type='text' readOnly={this.state.isReadOnly} />
                  )}
                </FormItem>
              </Col>
              <Col xs={24} sm={8}>
                <FormItem {...formItemLayout} label='菜单类型' >
                  {getFieldDecorator('func_type', {
                    rules: [{}],
                  })(
                    <Input type='text' readOnly={this.state.isReadOnly} />
                  )}

                </FormItem>
              </Col>
              <Col xs={24} sm={8}>
                <FormItem {...formItemLayout} label='父菜单' >
                  <Select name='func_pid' value={this.state.func_pid.toString()} style={{ width: 120 }} onChange={(value) => this.onSelectChange('func_pid', value)}>
                    {rootMenuItems}
                  </Select>
                </FormItem>
              </Col>
            </Row>
            <Row>
              <Col xs={24} sm={8}>
                <FormItem {...formItemLayout} label='菜单路径' >
                  {getFieldDecorator('func_url', {
                    rules: [{ required: true, message: '请输入菜单路径!' }],
                  })(
                    <Input type='text' readOnly={this.state.isReadOnly} />
                  )}

                </FormItem>
              </Col>
              <Col xs={24} sm={8}>
                <FormItem {...formItemLayout} label='菜单图标' >
                  {getFieldDecorator('func_icon', {
                    rules: [{}],
                  })(
                    <Input type='text' readOnly={this.state.isReadOnly} />
                  )}

                </FormItem>
              </Col>
            </Row>
            <Row>


              <Col xs={24} sm={8}>
                <FormItem {...formItemLayout} label="菜单描述">
                  {getFieldDecorator('func_desc', {
                    rules: [{ required: true, message: '请输入菜单描述!' }],
                  })(
                    <Input type='text' readOnly={this.state.isReadOnly} />
                  )}
                </FormItem>
              </Col>
              <Col xs={24} sm={8}>
                <FormItem {...formItemLayout} label='是否有效' >
                  <Select name='valid' value={this.state.valid.toString()} style={{ width: 120 }} onChange={(value) => this.onSelectChange('valid', value)}>
                    <Option value='1' >有效</Option>
                    <Option value='0' >无效</Option>
                  </Select>
                </FormItem>
              </Col>
            </Row>

          </Form>
        </Card >
      </div >
    );
  }
}
export default Form.create()(menuEdit);