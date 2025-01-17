import * as React from 'react';
import { Redirect, Route, Switch } from 'react-router';

import { AggregationPage } from './pages/AggregationPage';
import { ApplicationPage } from './pages/ApplicationPage';

interface Path {
    readonly path: string;
    readonly component: React.ComponentType;
}

export const linkTo = {
    application: (applicationId: string) => `/application/${applicationId}`,
    aggregation: () => '/aggregation'
};

const pathRoutes: Path[] = [
    {
        path: linkTo.aggregation(),
        component: AggregationPage
    },
    {
        path: linkTo.application(':applicationId'),
        component: ApplicationPage
    }
];

export const Routes: React.FunctionComponent<unknown> = _props => {
    return (
        <Switch>
            { pathRoutes.map(pathRoute => (
                <Route
                    key={ pathRoute.path }
                    component={ pathRoute.component }
                    path={ pathRoute.path }
                />
            )) }
            <Redirect to={ linkTo.aggregation() } />
        </Switch>
    );
};
