
import React from 'react';
import { HashRouter as Router, Switch, Redirect, Route, Link } from 'react-router-dom'
// 页面
import OrgManager from './orgManager.jsx';


class OrgManagerRouter extends React.Component {
    render() {
        return (
            <Switch>
                <Route path="/org/OrgManager" component={OrgManager} />
                <Redirect exact from="/org" to="/org/OrgManager" />
            </Switch>
        )
    }
}
export default OrgManagerRouter;