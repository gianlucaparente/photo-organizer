import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { PhotoOrganizerTagModule } from './tag/tag.module';
import { PhotoOrganizerPhotoModule } from './photo/photo.module';
/* jhipster-needle-add-entity-module-import - JHipster will add entity modules imports here */

@NgModule({
    imports: [
        PhotoOrganizerTagModule,
        PhotoOrganizerPhotoModule,
        /* jhipster-needle-add-entity-module - JHipster will add entity modules here */
    ],
    declarations: [],
    entryComponents: [],
    providers: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PhotoOrganizerEntityModule {}
