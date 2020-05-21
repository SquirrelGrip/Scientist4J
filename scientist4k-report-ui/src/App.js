import React, {Component} from 'react';
import {makeStyles} from '@material-ui/core/styles';
import ExperimentGrid from "./ExperimentGrid";
import Navigation from "./Navigation";
import {BrowserRouter as Router, Route, Switch} from "react-router-dom";
import ExperimentDetails from "./ExperimentDetails";

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
  },
}));
const classes = useStyles;

export default class App extends Component {

  render() {
    return (
      <div className={classes.root}>
        <Router forceRefresh={true}>
          <Navigation heading={'Scientist'} />
          <Switch>
            <Route path="/:experiment">
              <ExperimentDetails/>
            </Route>
            <Route path="/">
              <ExperimentGrid/>
            </Route>
          </Switch>
        </Router>
      </div>
    );
  }
}