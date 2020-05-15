import React, {useEffect, useState} from "react";
import axios from "axios";
import ExperimentSummary from "./ExperimentSummary";
import Grid from "@material-ui/core/Grid";
import Breadcrumbs from "@material-ui/core/Breadcrumbs";
import Typography from "@material-ui/core/Typography";

export default function ExperimentGrid() {
    const [experiments, setExperiments] = useState();

    useEffect(() => {
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
            .then(({data}) => {
                setExperiments(data);
            });
    }, []);

    return experiments ? (
        <div className="experiment-list">
          <Breadcrumbs maxItems={2} aria-label="breadcrumb">
            <Typography color="textPrimary">Experiments</Typography>
          </Breadcrumbs>
          <Grid container spacing={2} alignItems={'stretch'} direction={'column'}>
            {experiments.map(experiment => (
                <ExperimentSummary url={'http://localhost:8081/'} experiment={experiment} key={experiment.name}/>
            ))}
            </Grid>
        </div>
    ) : (
        <div>Loading...</div>
    );
};