import { Route } from '@angular/router';
import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { JhiPaginationUtil } from 'ng-jhipster';

import { HomeComponent } from './';

@Injectable()
export class HomeResolvePagingParams implements Resolve<any> {

    constructor(private paginationUtil: JhiPaginationUtil) {}

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const page = route.queryParams['page'] ? route.queryParams['page'] : '1';
        const sort = route.queryParams['sort'] ? route.queryParams['sort'] : 'id,asc';
        return {
            page: this.paginationUtil.parsePage(page),
            predicate: this.paginationUtil.parsePredicate(sort),
            ascending: this.paginationUtil.parseAscending(sort)
        };
    }
}

export const HOME_ROUTE: Route = {
    path: '',
    component: HomeComponent,
    resolve: {
        'pagingParams': HomeResolvePagingParams
    },
    data: {
        authorities: [],
        pageTitle: 'home.title'
    }
};
