import React, {useEffect, useState} from "react";
import axios from "axios";
import Experiment from "./Experiment";
import Grid from "@material-ui/core/Grid";

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
            <Grid container spacing={2} alignItems={'stretch'} direction={'column'}>
            {experiments.map(experiment => (
                <Experiment percent={'99%'} url={'http://localhost:8081/'} name={experiment} key={experiment}/>
            ))}
            </Grid>
        </div>
    ) : (
        <div>Loading...</div>
    );
};