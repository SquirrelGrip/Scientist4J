import React from 'react';
import {makeStyles} from '@material-ui/core/styles';
import ExperimentGrid from "./ExperimentGrid";
import Container from "@material-ui/core/Container";
import Navigation from "./Navigation";
import {BrowserRouter as Router, Switch, Route} from "react-router-dom";
import ExperimentDetails from "./ExperimentDetails";

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
  },
}));

export default function App() {
  const classes = useStyles();

  return (
    <div className={classes.root}>
      <Navigation heading={'Scientist'}/>
      <div style={{marginTop: '80px'}}>
        <Router>
          <div>
            <Switch>
              <Route path="/experiment/:experiment" children={<ExperimentDetails />} />
              <Route path="/">
                <Container maxWidth='xl'>
                  <ExperimentGrid/>
                </Container>
              </Route>
            </Switch>
          </div>
        </Router>
      </div>
    </div>
  );
}
