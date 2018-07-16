import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { Tag } from './tag.model';
import { TagPopupService } from './tag-popup.service';
import { TagService } from './tag.service';
import { Photo, PhotoService } from '../photo';
import { UserService, Principal, User } from '../../shared';

@Component({
    selector: 'jhi-tag-dialog',
    templateUrl: './tag-dialog.component.html'
})
export class TagDialogComponent implements OnInit {

    tag: Tag;
    isSaving: boolean;
    photos: Photo[];
    users: User[];
    parenttags: Tag[];
    parentTag: Tag;

    constructor(
        public activeModal: NgbActiveModal,
        private jhiAlertService: JhiAlertService,
        private tagService: TagService,
        private photoService: PhotoService,
        private principal: Principal,
        private eventManager: JhiEventManager,
        private userService: UserService
    ) {
    }

    ngOnInit() {
        this.isSaving = false;
        this.photoService.query()
            .subscribe((res: HttpResponse<Photo[]>) => { this.photos = res.body; }, (res: HttpErrorResponse) => this.onError(res.message));
        this.principal.identity().then((account) => {
            this.tag.user = account;
        });
        this.userService.query().subscribe((res: HttpResponse<User[]>) => {
            this.users = res.body;
        }, (res: HttpErrorResponse) => this.onError(res.message));

        this.tagService.query()
            .subscribe((res: HttpResponse<Tag[]>) => {
                this.parenttags = res.body;
                if (!this.tag.parentTag && this.parentTag) {
                    this.tag.parentTag = this.parentTag;
                }
            }, (res: HttpErrorResponse) => this.onError(res.message));
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.tag.id !== undefined) {
            this.subscribeToSaveResponse(
                this.tagService.update(this.tag));
        } else {
            this.subscribeToSaveResponse(
                this.tagService.create(this.tag));
        }
    }

    private subscribeToSaveResponse(result: Observable<HttpResponse<Tag>>) {
        result.subscribe((res: HttpResponse<Tag>) =>
            this.onSaveSuccess(res.body), (res: HttpErrorResponse) => this.onSaveError());
    }

    private onSaveSuccess(result: Tag) {
        this.eventManager.broadcast({ name: 'tagListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError() {
        this.isSaving = false;
    }

    private onError(error: any) {
        this.jhiAlertService.error(error.message, null, null);
    }

    trackPhotoById(index: number, item: Photo) {
        return item.id;
    }

    trackUserById(index: number, item: User) {
        return item.id;
    }

    trackTagById(index: number, item: Tag) {
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
}

@Component({
    selector: 'jhi-tag-popup',
    template: ''
})
export class TagPopupComponent implements OnInit, OnDestroy {

    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private tagPopupService: TagPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            const id = params['id'] ? params['id'] : undefined;
            const parentTagId = params['parentTagId'] ? params['parentTagId'] : undefined;
            this.tagPopupService.open(TagDialogComponent as Component, id, parentTagId);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}
