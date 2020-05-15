import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import ExperimentDetails from "./ExperimentDetails";
import * as serviceWorker from './serviceWorker';
import {BrowserRouter, Route} from "react-router-dom";

const routing = (
  <BrowserRouter>
    <div>
      <Route path="/" component={App} />
      <Route path="/contacts" component={ExperimentDetails} />
    </div>
  </BrowserRouter>
)
ReactDOM.render(routing, document.getElementById('root'))
//
// ReactDOM.render(
//   <React.StrictMode>
//     <App />
//   </React.StrictMode>,
//   document.getElementById('root')
// );

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
