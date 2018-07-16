import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { JhiPaginationUtil } from 'ng-jhipster';

import { UserRouteAccessService } from '../../shared';
import { TagComponent } from './tag.component';
import { TagDetailComponent } from './tag-detail.component';
import { TagPopupComponent } from './tag-dialog.component';
import { TagDeletePopupComponent } from './tag-delete-dialog.component';

@Injectable()
export class TagResolvePagingParams implements Resolve<any> {

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

export const tagRoute: Routes = [
    {
        path: 'tag',
        component: TagComponent,
        resolve: {
            'pagingParams': TagResolvePagingParams
        },
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'photoOrganizerApp.tag.home.title'
        },
        canActivate: [UserRouteAccessService]
    }, {
        path: 'tag/:id',
        component: TagDetailComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'photoOrganizerApp.tag.home.title'
        },
        canActivate: [UserRouteAccessService]
    }
];

export const tagPopupRoute: Routes = [
    {
        path: 'tag-new',
        component: TagPopupComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'photoOrganizerApp.tag.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'tag-new/:parentTagId',
        component: TagPopupComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'photoOrganizerApp.tag.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'tag/:id/edit',
        component: TagPopupComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'photoOrganizerApp.tag.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'tag/:id/delete',
        component: TagDeletePopupComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'photoOrganizerApp.tag.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    }
];
