import React, {Component} from "react";
import axios from "axios";
import ExperimentSummary from "./ExperimentSummary";
import Grid from "@material-ui/core/Grid";
import Container from "@material-ui/core/Container";

export default class ExperimentGrid extends Component {
  state = {
    experiments: null
  }

  componentDidMount() {
    axios
      .get(
        "http://localhost:9080/api/v1/experiments",
        {
          headers: {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Credentials': true
          }
        }
      )
      .then(res => {
        const experiments = res.data;
        this.setState({experiments});
      });
  }

  render() {
    return this.state.experiments ? (
      <Container maxWidth='xl'>
        <div className="experiment-list">
          <Grid container spacing={2} alignItems={'stretch'} direction={'column'}>
            {this.state.experiments.map(experiment => (
              <ExperimentSummary url={'http://localhost:8081/'} experiment={experiment} key={experiment.name}/>
            ))}
          </Grid>
        </div>
      </Container>
    ) : (
      <div>Loading...</div>
    );
  }
}