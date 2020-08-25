import { Route, Switch } from "react-router-dom";
import React from "react";
import HomePage from "../components/pages/HomePage/HomePage.js";
import Auth from "../components/pages/FormRegister/Auth.js";
import Profile from "../components/pages/ProfilePage/profile";

export default () => {
  return (
    <Switch>
      <Route path="/" component={HomePage} exact />
      <Route path="/profile/" component={Profile} />
      <Route path="/registration/" component={Auth} />
      <Route path="/registration/register" exact component={Auth} />
    </Switch>
  );
};
