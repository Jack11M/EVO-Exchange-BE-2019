import React from "react";
import { Route, Switch } from "react-router-dom";
import MyActivity from "./my_activity";
import MyProfile from "./my_profile";
import MyFavorites from "./my_favorites";
import MySettings from "./my_settings";

const RouterTabs = props => {
  const { path } = props;
  return (
    <Switch>
      <Route path={`${path}`} component={MyActivity} exact />
      <Route path={`${path}my_profile`} component={MyProfile} />
      <Route path={`${path}my_favorites`} component={MyFavorites} />
      <Route path={`${path}my_settings`} component={MySettings} />
      <Route path={`${path}exit`} component={MySettings} />
    </Switch>
  );
};

export default RouterTabs;
