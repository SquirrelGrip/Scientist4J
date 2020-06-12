import React, {Component} from 'react';
import axios from "axios";
import { withRouter } from "react-router-dom";
import ExperimentDetail from "./ExperimentDetail";

class ExperimentDetails extends Component {
  state = {
    experimentDetails: null
  }

  componentDidMount() {
    const experiment = this.props.match.params.experiment;
    axios
      .get(
        "http://localhost:9080/api/v1/experiment/" + experiment + "/urls",
        {
          headers: {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Credentials': true
          }
        }
      )
      .then(res => {
        const experimentDetails = res.data;
        this.setState({ experimentDetails });
      });
  }

  render() {
    const {experimentDetails} = this.state;
    return experimentDetails ? (
      <div key={experimentDetails.name}>
        {experimentDetails.urls.map(url => (
          <ExperimentDetail url={url} key={url.method + "_" + url.uri}/>
        ))};
      </div>
    ) : (
      <div>Loading...</div>
    );
  }
}

export default withRouter(ExperimentDetails);