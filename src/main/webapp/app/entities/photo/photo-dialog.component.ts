import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { Photo } from './photo.model';
import { PhotoPopupService } from './photo-popup.service';
import { PhotoService } from './photo.service';
import { Tag, TagService } from '../tag';
import { User, UserService, Principal, FILE_EXTENSIONS_ALLOWED } from '../../shared';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
    selector: 'jhi-photo-dialog',
    templateUrl: './photo-dialog.component.html'
})
export class PhotoDialogComponent implements OnInit {

    photo: Photo;
    tagSelected: Tag;
    isSaving: boolean;
    tags: Tag[];
    users: User[];

    constructor(
        public activeModal: NgbActiveModal,
        private jhiAlertService: JhiAlertService,
        private photoService: PhotoService,
        private tagService: TagService,
        private userService: UserService,
        private eventManager: JhiEventManager,
        private principal: Principal
    ) {
    }

    ngOnInit() {

        this.isSaving = false;

        this.tagService.query().subscribe((res: HttpResponse<Tag[]>) => {
            this.tags = res.body;
            if (this.tagSelected) {
                if (!this.photo.tags) {
                    this.photo.tags = [];
                }
                this.photo.tags.push(this.tagSelected);
            }
        }, (res: HttpErrorResponse) => this.onError(res.message));

        this.userService.query().subscribe((res: HttpResponse<User[]>) => {
            this.users = res.body;
        }, (res: HttpErrorResponse) => this.onError(res.message));

        this.principal.identity().then((account) => {
            this.photo.user = account;
        });

    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;

        const formData = new FormData();

        if (this.photo.id) {
            formData.append('photoId', String(this.photo.id));
        }

        formData.append('image', this.photo.image);
        formData.append('tagIds', this.photo.tags.map((t: Tag) => t.id).join(','));
        formData.append('userId', String(this.photo.user.id));

        this.photoService.save(formData, !this.photo.id).subscribe((photo: Photo) => {

            this.eventManager.broadcast({ name: 'photoListModification', content: 'OK'});
            this.isSaving = false;
            this.activeModal.dismiss(photo);

        }, (res: HttpErrorResponse) => this.onSaveError());

    }

    private onSaveError() {
        this.isSaving = false;
    }

    private onError(error: any) {
        this.jhiAlertService.error(error.message, null, null);
    }

    trackTagById(index: number, item: Tag) {
        return item.id;
    }

    trackUserById(index: number, item: User) {
        return item.id;
    }

    getSelected(selectedVals: Array<any>, option: any) {
        if (selectedVals) {
            for (let i = 0; i < selectedVals.length; i++) {
                if (option.id === selectedVals[i].id) {
                    return selectedVals[i];
                }
            }
        }
        return option;
    }

    uploadPhoto(evt: any) {

        this.photo.image = evt.target.files[0];

        const nameSplit = this.photo.image.name.split('.');

        this.photo.fileName = nameSplit[0];
        this.photo.type = nameSplit[1].toLowerCase();
        this.photo.dateCreated = this.photo.image.lastModifiedDate.toISOString() || new Date().toISOString();

        if (FILE_EXTENSIONS_ALLOWED.indexOf(this.photo.type) > -1) {

            const reader = new FileReader();
            reader.readAsDataURL(this.photo.image);
            reader.onload = (event: any) => {
                this.photo.preview = event.target.result;
            };

        } else {
            this.jhiAlertService.error('photoOrganizerApp.photo.extError', null, null);
        }

    }

}

@Component({
    selector: 'jhi-photo-popup',
    template: ''
})
export class PhotoPopupComponent implements OnInit, OnDestroy {

    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private photoPopupService: PhotoPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            const id = params['id'] ? params['id'] : undefined;
            const tagId = params['tagId'] ? params['tagId'] : undefined;
            this.photoPopupService.open(PhotoDialogComponent as Component, id, tagId);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
