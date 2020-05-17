import React, {Component} from 'react';
import {makeStyles} from '@material-ui/core/styles';
import ExperimentGrid from "./ExperimentGrid";
import Navigation from "./Navigation";
import {BrowserRouter as Router, Route, Switch, useParams} from "react-router-dom";
import ExperimentDetails from "./ExperimentDetails";

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
  },
}));
const classes = useStyles;

export default class App extends Component {
  state = {
    location: window.location
  }

  // onLocationChange = () => {
  //   this.setState({location: window.location});
  // }

  render() {
    return (
      <div className={classes.root}>
        <Navigation heading={'Scientist'} location={this.state.location}/>
        <Router>
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