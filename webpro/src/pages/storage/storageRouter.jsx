import React from 'react';
import { HashRouter as Router, Switch, Redirect, Route, Link } from 'react-router-dom'

import Loadable from 'react-loadable';
import loading from '@/utils/loading.jsx'

const inStorageList = Loadable({
    loader: () => import(/* webpackChunkName: "inStorageList" */ './inStorage/inStorageList.jsx'),
    loading: loading,
    delay: 3000
});
const storageList = Loadable({
    loader: () => import(/* webpackChunkName: "storageList" */ './storage/storageList.jsx'),
    loading: loading,
    delay: 3000
});


export default class storageRouter extends React.Component {
    render() {
        return (
            <Switch>
                <Route path="/storage/inStorageList" component={inStorageList} />
                <Route path="/storage/storageList" component={storageList} />
            </Switch>
        )
    }
}