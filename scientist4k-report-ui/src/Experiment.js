import React from 'react';
import {makeStyles} from '@material-ui/core/styles';
import Grid from '@material-ui/core/Grid';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Box from "@material-ui/core/Box";

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

export default function Experiment(prop) {
  const classes = useStyles();
  const passPercentage = (prop.experiment.pass / prop.experiment.count * 100).toFixed(0)

  function onExperimentClick() {
    console.log(prop.experiment)
  }

  return (
    <Paper className={classes.paper} variant={'elevation'} onClick={onExperimentClick}>
      <Grid item xs={12} sm container>
        <Grid item xs container direction="column" spacing={2} key={prop.experiment.name}>
          <Grid item xs>
            <Typography gutterBottom variant="subtitle1">{prop.experiment.name}</Typography>
            <Typography variant="body2" gutterBottom>{prop.url}</Typography>
          </Grid>
          <Grid item>
            <Typography variant="body2" style={{cursor: 'pointer'}}>Remove</Typography>
          </Grid>
        </Grid>
        <Grid item>
          <Box fontWeight="fontWeightBold">{passPercentage}%</Box>
        </Grid>
      </Grid>
    </Paper>
  );
}
