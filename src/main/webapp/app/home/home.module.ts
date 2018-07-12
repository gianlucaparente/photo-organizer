import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { PhotoOrganizerSharedModule } from '../shared';

import { HOME_ROUTE, HomeComponent, HomeResolvePagingParams } from './';

@NgModule({
    imports: [
        PhotoOrganizerSharedModule,
        RouterModule.forChild([ HOME_ROUTE ])
    ],
    declarations: [
        HomeComponent,
    ],
    entryComponents: [
    ],
    providers: [
        HomeResolvePagingParams
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PhotoOrganizerHomeModule {}
