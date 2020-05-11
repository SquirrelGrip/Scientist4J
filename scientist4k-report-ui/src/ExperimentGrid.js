import React from 'react';
import {makeStyles} from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Experiment from "./Experiment";

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
  },
}));

export default function ExperimentGrid() {
  const classes = useStyles();

  return (
    <div className={classes.root}>
        <Grid container spacing={2} alignItems={'stretch'} direction={'column'}>
          <Experiment percent={'99%'} url={'http://localhost:8081/'} name={'Experiment01'} />
          <Experiment percent={'98%'} url={'http://localhost:8082/'} name={'Experiment02'} />
          <Experiment percent={'98%'} url={'http://localhost:8082/'} name={'Experiment03'} />
          <Experiment percent={'98%'} url={'http://localhost:8082/'} name={'Experiment04'} />
          <Experiment percent={'98%'} url={'http://localhost:8082/'} name={'Experiment05'} />
          <Experiment percent={'98%'} url={'http://localhost:8082/'} name={'Experiment06'} />
          <Experiment percent={'98%'} url={'http://localhost:8082/'} name={'Experiment07'} />
          <Experiment percent={'98%'} url={'http://localhost:8082/'} name={'Experiment08'} />
          <Experiment percent={'98%'} url={'http://localhost:8082/'} name={'Experiment09'} />
          <Experiment percent={'98%'} url={'http://localhost:8082/'} name={'Experiment10'} />
          <Experiment percent={'98%'} url={'http://localhost:8082/'} name={'Experiment11'} />
          <Experiment percent={'98%'} url={'http://localhost:8082/'} name={'Experiment12'} />
        </Grid>
    </div>
  );
}
