import React, {useEffect, useState} from 'react';
import Typography from '@material-ui/core/Typography';
import Breadcrumbs from "@material-ui/core/Breadcrumbs";
import {useHistory, useParams} from "react-router-dom";
import Link from "@material-ui/core/Link";
import axios from "axios";

export default function ExperimentDetails() {
  const history = useHistory();
  let {experiment} = useParams();
  const [experimentDetails, setExperimentDetails] = useState();

  useEffect(() => {
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
      .then(({data}) => {
        console.log(data);
        setExperimentDetails(data);
      });
  }, []);


  function onExperimentsClick() {
    history.push("/");
  }

  return experimentDetails ? (
    <div>
      <Breadcrumbs maxItems={2} aria-label="breadcrumb">
        <Link color="inherit" href="#" onClick={onExperimentsClick}>Experiments</Link>
        <Typography color="textPrimary">{experimentDetails.name}</Typography>
      </Breadcrumbs>
      {experimentDetails.urls.map(url => (
        <div><span>{url.method}</span> <span>{url.uri}</span></div>
      ))}
    </div>
  ) : (
    <div>Loading...</div>
  );
}
