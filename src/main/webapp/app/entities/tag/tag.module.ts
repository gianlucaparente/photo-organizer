import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';

import { PhotoOrganizerSharedModule } from '../../shared';
import { PhotoOrganizerAdminModule } from '../../admin/admin.module';
import {
    TagService,
    TagPopupService,
    TagComponent,
    TagDetailComponent,
    TagDialogComponent,
    TagPopupComponent,
    TagDeletePopupComponent,
    TagDeleteDialogComponent,
    tagRoute,
    tagPopupRoute,
    TagResolvePagingParams,
} from './';

const ENTITY_STATES = [
    ...tagRoute,
    ...tagPopupRoute,
];

@NgModule({
    imports: [
        PhotoOrganizerSharedModule,
        PhotoOrganizerAdminModule,
        RouterModule.forChild(ENTITY_STATES)
    ],
    declarations: [
        TagComponent,
        TagDetailComponent,
        TagDialogComponent,
        TagDeleteDialogComponent,
        TagPopupComponent,
        TagDeletePopupComponent,
    ],
    entryComponents: [
        TagComponent,
        TagDialogComponent,
        TagPopupComponent,
        TagDeleteDialogComponent,
        TagDeletePopupComponent,
    ],
    providers: [
        TagService,
        TagPopupService,
        TagResolvePagingParams,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PhotoOrganizerTagModule {}
