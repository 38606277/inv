import React from 'react';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Input, Select, Button, Card, Row, Col, Tree, message, TreeSelect } from 'antd';
import LocalStorge from '@/utils/LogcalStorge.jsx';
import HttpService from '@/utils/HttpService.jsx';
// import EditableTree from './EditableTree.js';
const localStorge = new LocalStorge();
const FormItem = Form.Item;
const { TreeNode } = Tree;
const key = "children";
function parseJson(arr) {
  arr = arr.slice()
  function toParse(arr) {
    arr.forEach(function (item) {
      var value = item.children;
      for (var j in item) {
        if (j == 'id') {
          //把要删除menuBnt的值拿出来赋到新的key中
          item["value"] = item.id;
          //删除旧的menuBnt
          delete item.id;
        }
        if (j == 'name') {
          //把要删除menuBnt的值拿出来赋到新的key中
          item["title"] = item.name;
          //删除旧的menuBnt
          delete item.name;
        }
      }
      toParse(value);
    })
    return arr
  }
  return toParse(arr)
}
class OrgManager extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      action: 'create',
      treeData: [],
      checkedKeys: [],
      selectedKeys: [],
      expandedKeys: [],
      autoExpandParent: true,
      options: [],
      org_id: '',
      org_num: '',
      org_name: '',
      org_type: '',
      address: '',
      org_pid: '0',
    };
  }

  componentDidMount() {
    //this.loadOrgData('0');
    this.getOrgTree();
  }

  getOrgTree() {
    HttpService.post('reportServer/org/getOrgTree', JSON.stringify({}))
      .then(res => {
        if (res.resultCode == "1000") {
          let treeDataList = JSON.parse(JSON.stringify(res.data));
          this.setState({
            treeData: res.data,
            options: parseJson(treeDataList)
          });
        }
        else {
          message.error(res.message);
        }
      });

  }




  loadOrgData(org_pid) {
    let param = {
      org_pid: org_pid,
    }
    HttpService.post('reportServer/org/listOrgTreeByOrgPid', JSON.stringify(param))
      .then(res => {
        if (res.resultCode == "1000") {
          this.setState({
            treeData: res.data,
          });
          if (0 < res.data.length) {
            this.setState({
              action: 'edit'
            })
            this.props.form.setFieldsValue(res.data[0]);
          } else {
            this.props.form.setFieldsValue({ org_pid: 0 });
          }

        }
        else {
          message.error(res.message);
        }

      });
  }



  onLoadData = treeNode =>
    new Promise(resolve => {
      if (treeNode.props.children) {
        resolve();
        return;
      }
      console.log('treeNode.props.dataRef : ', treeNode.props.dataRef)
      let param = {
        org_pid: treeNode.props.dataRef.org_id
      }

      HttpService.post('reportServer/org/listOrgTreeByOrgPid', JSON.stringify(param))
        .then(res => {
          if (res.resultCode == "1000") {
            treeNode.props.dataRef.children = res.data;
            this.setState({
              treeData: [...this.state.treeData],
            });
            resolve();
          }
          else {
            message.error(res.message);
          }

        });


    });


  onSelect = (selectedKeys, info) => {
    if (info.selected) {
      let param = {
        org_id: selectedKeys[0]
      }
      HttpService.post('reportServer/org/getOrgByID', JSON.stringify(param))
        .then(res => {
          if (res.resultCode == "1000") {
            this.props.form.setFieldsValue(res.data);
          }
          else {
            message.error(res.message);
          }
        });
    }
    console.log('onSelect', info);
    console.log('selectedKeys', selectedKeys);
    this.setState({ selectedKeys });
  };

  /** 
   * 选中节点 并递归选中子节点
   */
  onCheck = (checkedKeys, info) => {
    if (info.checked) { //选中
      this.arrayAddItem(this.state.checkedKeys, info.node.props.dataRef.id);
      this.recursiveChecked(info.node.props.dataRef.children, true);

    } else {//取消选中
      this.arrayRemoveItem(this.state.checkedKeys, info.node.props.dataRef.id)
      this.recursiveChecked(info.node.props.dataRef.children, false);
    }
    this.setState({ checkedKeys: this.state.checkedKeys });
    console.log('onCheck', checkedKeys, info);
    console.log('this.state.checkedKeys', this.state.checkedKeys)


  }

  //数组移除
  arrayRemoveItem(array, id) {
    for (let index in array) {
      let item = array[index];
      if (item == id) {
        array.splice(index, 1);
        console.log('移除', id)
      }
    }
  }
  //数组添加
  arrayAddItem(array, id) {
    let boo = true;
    for (let index in array) {
      let item = array[index];
      if (item == id) {
        boo = false;
        break;
      }
    }
    if (boo) {
      array.push(id);
      console.log('添加', id);
    }
  }

  //递归选中子节点
  recursiveChecked(children, isSelect) {
    console.log('recursiveChecked', children)
    for (let i in children) {
      let child = children[i];
      if (isSelect) { //添加
        this.arrayAddItem(this.state.checkedKeys, child.id);
      } else {//删除
        this.arrayRemoveItem(this.state.checkedKeys, child.id)
      }
      this.recursiveChecked(child.children, isSelect);
    }

  }


  /**
   * 删除组织
   */
  onDeleteOrgClick = () => {
    if (0 < this.state.checkedKeys.length) {

      let ids = [];
      for (let i in this.state.checkedKeys) {
        ids.push({ org_id: this.state.checkedKeys[i] });
      }
      let params = { org_ids: ids };

      HttpService.post('reportServer/org/deleteByOrgIds', JSON.stringify(params))
        .then(res => {
          if (res.resultCode == "1000") {
            // 删除完成需要刷新 
            message.success("删除成功");
            this.getOrgTree();
          }
          else {
            message.error(res.message);
          }
        });
    } else {
      message.error('请选择需要删除的组织')
    }
  }
  //编辑字段对应值
  onValueChange(e) {
    let name = e.target.name,
      value = e.target.value.trim();
    this.setState({ [name]: value });

    this.props.form.setFieldsValue({ [name]: value });

  }
  /**
   * 添加组织
   */
  async onAddOrgClick() {

    //let org_id = this.state.selectedKeys[0];
    //新境节点并，提交
    this.props.form.validateFieldsAndScroll((err, values) => {
      if (!err) {
        let formInfo = this.props.form.getFieldsValue();
        if (null != formInfo.org_id && "" != formInfo.org_id) {
          message.warning("数据已存在，不能再次新增，请点击【保存】按钮！");
          return false;
        }
        if (undefined == formInfo.org_id) {
          formInfo.org_id = '';
        }
        HttpService.post('reportServer/org/createOrg', JSON.stringify(formInfo))
          .then(res => {
            if (res.resultCode == "1000") {
              message.success("创建成功");
              this.getOrgTree();
              //返回ID,选中ID
              // this.setState({ selectedKeys: [res.data] });
              // this.arrayAddItem(this.state.expandedKeys, org_id)
            }
            else {
              message.error(res.message);
            }
          });
      }
    });
    // await this.getOrgTree();
    //  // selectedKeys=["35"];
    //  let s=this.state.selectedKeys;
    //   this.setState({selectedKeys:s});
    //this.onSelect(this.state.selectedKeys, null);

    //选中当前节点
    // this.setState({ selectedKeys });


    // await this.loadOrgData('0');
    //加载当前节点的父
    // await this.loadOrgData('3');

    //

    // org_type,org_num,org_name,org_pid
    // let a= this.parseJson(this.state.treeData,3);
    // alert(stringify(a));
    //this.state.treeData.find()
    // if (this.state.selectNode) {

    //   this.props.form.setFieldsValue({ org_pid: this.state.selectNode.org_id });
    //   this.setState({
    //     action: 'create'
    //   })
    // } else {
    //   message.error('请选择上级组织')
    // }
  }
  parseJson = (jsonObj, id) => {
    // 循环所有键
    for (var v in jsonObj) {
      var element = jsonObj[v]
      // 1.判断是对象或者数组
      if (typeof (element) == 'object') {
        this.parseJson(element, id)
      } else {
        if (element == id) {
          console.log(v + ':' + id)
          return element;
        }
      }
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


  onSaveOrgClick = () => {
    this.props.form.validateFieldsAndScroll((err, values) => {
      if (!err) {
        let formInfo = this.props.form.getFieldsValue();
        HttpService.post('reportServer/org/updateOrgByOrgId', JSON.stringify(formInfo))
          .then(res => {
            if (res.resultCode == "1000") {
              //修改成功需要刷新列表
              message.success("保存成功");
              this.getOrgTree();
            }
            else {
              message.error(res.message);
            }
          });
      }
    });
  }

  resetForm = () => {
    const { form } = this.props;
    form.resetFields();
    console.log(this.props.form.getFieldsValue());
  }
  renderTreeNodes = data =>
    data.map(item => {
      if (item.children) {
        return (
          <TreeNode title={item.name} key={item.id} dataRef={item}>
            {this.renderTreeNodes(item.children)}
          </TreeNode>
        );
      }
      return <TreeNode key={item.id} title={item.name} isLeaf={item.isLeaf == 1} dataRef={item} />;
    });

  onSelectChange = value => {
    this.setState({ org_id: value == undefined ? '0' : value });
  };

  render() {
    const { getFieldDecorator } = this.props.form;
    const formItemLayout = {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 8 },
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 16 },
      },
    };
    const tailFormItemLayout = {
      wrapperCol: {
        xs: {
          span: 24,
          offset: 0,
        },
        sm: {
          span: 16,
          offset: 8,
        },
      },
    };



    return (

      <div id="page-wrapper">
        <Col xs={24} sm={6}>
          <Card title="组织架构">
            {/* <EditableTree/> */}
            <Tree style={{ height: '600px', overflow: 'auto' }}
              autoExpandParent={this.state.autoExpandParent}
              // loadData={this.onLoadData} 
              checkable
              onExpand={this.onExpand}
              expandedKeys={this.state.expandedKeys}
              // autoExpandParent={this.state.autoExpandParent}
              onCheck={this.onCheck}
              checkedKeys={this.state.checkedKeys}
              onSelect={this.onSelect}
              selectedKeys={this.state.selectedKeys}
              checkStrictly
            >
              {this.renderTreeNodes(this.state.treeData)}
            </Tree>
          </Card>
        </Col>

        <Col xs={24} sm={18}>
          <Card title={<b> {this.state.action == 'create' ? '新增组织' : '编辑组织'} </b>} bordered={false} extra={<span>
            <Button type="primary" style={{ marginLeft: '10px' }} onClick={() => this.resetForm()}>重置</Button>
            <Button type="primary" style={{ marginLeft: '10px' }} onClick={() => this.onAddOrgClick()}>新增</Button>
            <Button type="primary" style={{ marginLeft: '10px' }} onClick={() => this.onDeleteOrgClick()}>删除</Button>
            <Button type="primary" style={{ marginLeft: '10px' }} onClick={() => this.onSaveOrgClick()}>保存</Button>
          </span>} bodyStyle={{ paddingBottom: '0px' }}>
            <Form >

              <FormItem style={{ display: 'none' }}>
                {getFieldDecorator('org_id')(
                  <Input type='text' />
                )}
              </FormItem>

              {/* <FormItem style={{ display: 'none' }}>
                {getFieldDecorator('org_pid')(
                  <Input type='text' />
                )}
              </FormItem> */}

              <Row>
                <Col xs={24} sm={12}>
                  <FormItem {...formItemLayout} label="组织编号">
                    {getFieldDecorator('org_num', {
                      rules: [{ required: true, message: '请输入组织编号' }],
                    })(
                      <Input type='text' name='org_num' />
                    )}
                  </FormItem>
                </Col>
                <Col xs={24} sm={12}>
                  <FormItem {...formItemLayout} label="组织名称">
                    {getFieldDecorator('org_name', {
                      rules: [{ required: true, message: '请输入组织名称' }],
                    })(
                      <Input type='text' name='org_name' />
                    )}
                  </FormItem>
                </Col>
              </Row>

              <Row>
                <Col xs={24} sm={12}>
                  <FormItem {...formItemLayout} label="组织类别">
                    {getFieldDecorator('org_type', {
                      rules: [{ required: true, message: '请输入组织类别' }],
                    })(
                      <Input type='text' name='org_type' />
                    )}
                  </FormItem>
                </Col>
                <Col xs={24} sm={12}>
                  <FormItem {...formItemLayout} label="地址信息">
                    {getFieldDecorator('address', {
                      rules: [{ required: true, message: '请输入地址信息' }],
                    })(
                      <Input type='text' name='address' />
                    )}
                  </FormItem>
                </Col>
              </Row>
              <Row>
                <Col xs={24} sm={12}>
                  <FormItem {...formItemLayout} label="父节点">
                    {getFieldDecorator('org_pid', {
                      rules: [{ required: false, message: '请选择父节点' }],
                    })(
                      <TreeSelect
                        allowClear="true"
                        style={{ width: '100%' }}
                        value={this.state.org_pid}
                        dropdownStyle={{ maxHeight: 260, overflow: 'auto' }}
                        treeData={this.state.options}
                        placeholder="请选择父节点"
                        treeDefaultExpandAll
                        onChange={this.onSelectChange}
                      />
                    )}
                  </FormItem>
                </Col>
              </Row>
            </Form>
          </Card>
        </Col>

      </div>
    )


  }


}
export default Form.create()(OrgManager);