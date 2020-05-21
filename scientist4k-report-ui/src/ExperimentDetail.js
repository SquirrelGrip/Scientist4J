import React from 'react';
import {makeStyles} from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import {useHistory} from "react-router-dom";

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
  },
  paper: {
    padding: theme.spacing(2),
    margin: '8px',
    maxWidth: '99%',
  },
}));

export default function ExperimentDetail(prop) {
  const classes = useStyles();
  const history = useHistory();

  function onExperimentDetailClick() {
    history.push(window.location.pathname + "/" + prop.url.method + "_" + prop.url.uri);
  }

  const key = prop.url.method + "_" + prop.url.uri;
  const name = prop.url.method + " " + prop.url.uri;
  const count = prop.url.passCount + prop.url.failCount;
  const passPercentage = (prop.url.passCount / count * 100).toFixed(0)

  return (
    <Paper className={classes.paper} variant={'elevation'} onClick={onExperimentDetailClick}>
      <Grid item xs={12} sm container>
        <Grid item xs container direction="row" justify="space-between" alignItems="flex-start" spacing={1} key={key + "_name"}>
          <Grid item xs={6}>
            <Typography gutterBottom variant="subtitle1" align="left">{name}</Typography>
          </Grid>
          <Grid item xs={2}>
            <Typography align="right" gutterBottom variant="subtitle1">{prop.url.passCount}/{count}</Typography>
          </Grid>
          <Grid item xs={3}>
            <Typography align="right">{passPercentage}%</Typography>
          </Grid>
        </Grid>
      </Grid>
    </Paper>
  );
}
