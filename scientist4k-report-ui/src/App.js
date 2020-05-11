import React from 'react';
import {makeStyles} from '@material-ui/core/styles';
import ExperimentGrid from "./ExperimentGrid";
import Container from "@material-ui/core/Container";
import Navigation from "./Navigation";

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
  },
}));

export default function App() {
  const classes = useStyles();

  return (
    <div className={classes.root}>
      <Navigation heading={'Experiments'}/>
      <div style={{marginTop: '80px'}}>
        <Container maxWidth='xl'>
          <ExperimentGrid/>
        </Container>
      </div>
    </div>
  );
}
