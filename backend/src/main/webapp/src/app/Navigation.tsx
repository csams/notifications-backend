import { Nav, NavExpandable, NavGroup, NavItem, NavItemSeparator, NavList } from '@patternfly/react-core';
import * as React from 'react';
import { Link, useRouteMatch } from 'react-router-dom';

import { linkTo } from '../Routes';

type EnhancedNavItemProps = {
    to: string;
}

const EnhancedNavItem: React.FunctionComponent<EnhancedNavItemProps> = props => {
    const match = useRouteMatch({
        path: props.to,
        exact: true
    });

    return (
        <NavItem isActive={ !!match }>
            <Link to={ props.to }>
                { props.children }
            </Link>
        </NavItem>
    );
};

export interface NavigationProps {
    bundles: Array<any>;
}

export const Navigation: React.FunctionComponent<NavigationProps> = props => {
    return (
        <Nav>
            <NavList>
                <NavGroup title="Bundles">
                    { props.bundles.map(b => (
                        <NavExpandable key={ b.id } title={ b.display_name }>
                            { b.applications.map((a: any) => (
                                <EnhancedNavItem key={ a.id } to={ linkTo.application(a.id) }>{ a.display_name }</EnhancedNavItem>
                            )) }
                        </NavExpandable>
                    )) }
                </NavGroup>
                <NavItemSeparator />
                <EnhancedNavItem to={ linkTo.aggregation() }>
                    Aggregation
                </EnhancedNavItem>
            </NavList>
        </Nav>
    );
};
