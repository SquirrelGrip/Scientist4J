import Breadcrumbs from "@material-ui/core/Breadcrumbs";
import {Link as RouterLink, withRouter} from "react-router-dom";
import Typography from "@material-ui/core/Typography";
import React, {Component} from "react";

class SimpleBreadcrumbs extends Component {
  componentWillMount() {
    this.unlisten = this.props.history.listen((location, action) => {
      console.log("on route change " + location);
    });
  }

  componentWillUnmount() {
    this.unlisten();
  }

  render() {
    const pathnames = window.location.pathname.split('/').filter(x => x);
    return (
      <Breadcrumbs aria-label="Breadcrumb">
        {pathnames[0] ? (
          <RouterLink color="inherit" to={"/"} key={"/"}>Experiments</RouterLink>
        ) : (
          <Typography color="textPrimary" key={"/"}>Experiments</Typography>
        )}
        {pathnames.map((value, index) => {
          const last = index === pathnames.length - 1;
          const to = `/${pathnames.slice(0, index + 1).join('/')}`;
          return last ? (
            <Typography color="textPrimary" key={to}>{value}</Typography>
          ) : (
            <RouterLink color="inherit" to={to} key={to}>{value}</RouterLink>
          );
        })}
      </Breadcrumbs>
    );
  }
}

export default withRouter(SimpleBreadcrumbs);