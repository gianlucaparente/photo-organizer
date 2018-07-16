import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { JhiPaginationUtil } from 'ng-jhipster';

import { UserRouteAccessService } from '../../shared';
import { PhotoComponent } from './photo.component';
import { PhotoDetailComponent } from './photo-detail.component';
import { PhotoPopupComponent } from './photo-dialog.component';
import { PhotoDeletePopupComponent } from './photo-delete-dialog.component';

@Injectable()
export class PhotoResolvePagingParams implements Resolve<any> {

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

export const photoRoute: Routes = [
    {
        path: 'photo',
        component: PhotoComponent,
        resolve: {
            'pagingParams': PhotoResolvePagingParams
        },
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'photoOrganizerApp.photo.home.title'
        },
        canActivate: [UserRouteAccessService]
    }, {
        path: 'photo/:id',
        component: PhotoDetailComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'photoOrganizerApp.photo.home.title'
        },
        canActivate: [UserRouteAccessService]
    }
];

export const photoPopupRoute: Routes = [
    {
        path: 'photo-new',
        component: PhotoPopupComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'photoOrganizerApp.photo.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'photo-new/:tagId',
        component: PhotoPopupComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'photoOrganizerApp.photo.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'photo/:id/edit',
        component: PhotoPopupComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'photoOrganizerApp.photo.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    },
    {
        path: 'photo/:id/delete',
        component: PhotoDeletePopupComponent,
        data: {
            authorities: ['ROLE_USER'],
            pageTitle: 'photoOrganizerApp.photo.home.title'
        },
        canActivate: [UserRouteAccessService],
        outlet: 'popup'
    }
];
