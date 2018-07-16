import { Injectable, Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { HttpResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { Photo } from './photo.model';
import { PhotoService } from './photo.service';
import { Tag } from '../tag/tag.model';
import { TagService } from '../tag/tag.service';
import { Subscription } from 'rxjs/Subscription';

@Injectable()
export class PhotoPopupService {
    private ngbModalRef: NgbModalRef;
    private subscription: Subscription;

    constructor(
        private datePipe: DatePipe,
        private modalService: NgbModal,
        private router: Router,
        private photoService: PhotoService,
        private tagService: TagService,
        private route: ActivatedRoute

    ) {
        this.ngbModalRef = null;
    }

    open(component: Component, id?: number | any, tagId?: number): Promise<NgbModalRef> {
        return new Promise<NgbModalRef>((resolve, reject) => {
            const isOpen = this.ngbModalRef !== null;
            if (isOpen) {
                resolve(this.ngbModalRef);
            }

            const promises: any[] = [];
            if (id) {
                promises.push(this.photoService.find(id).toPromise());
            }
            if (tagId) {
                promises.push(this.tagService.find(tagId).toPromise());
            }

            if (promises.length > 0) {

                Promise.all(promises)
                    .then((responses: HttpResponse<any>[]) => {

                        let photo: Photo = undefined;
                        let tag: Tag = undefined;

                        if (id && tagId) {
                            photo = responses[0].body;
                            photo.dateCreated = this.datePipe.transform(photo.dateCreated, 'yyyy-MM-ddTHH:mm:ss');
                            tag = responses[1].body;
                        } else if(id) {
                            photo = responses[0].body;
                            photo.dateCreated = this.datePipe.transform(photo.dateCreated, 'yyyy-MM-ddTHH:mm:ss');
                        } else {
                            tag = responses[0].body;
                        }

                        this.ngbModalRef = this.photoModalRef(component, photo || new Photo(), tag);
                        resolve(this.ngbModalRef);
                    });

            } else {
                // setTimeout used as a workaround for getting ExpressionChangedAfterItHasBeenCheckedError
                setTimeout(() => {
                    this.ngbModalRef = this.photoModalRef(component, new Photo(), undefined);
                    resolve(this.ngbModalRef);
                }, 0);
            }

        });
    }

    photoModalRef(component: Component, photo: Photo, tagSelected: Tag): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.photo = photo;
        modalRef.componentInstance.tagSelected = tagSelected;
        modalRef.result.then((result) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true, queryParamsHandling: 'merge' });
            this.ngbModalRef = null;
        }, (reason) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true, queryParamsHandling: 'merge' });
            this.ngbModalRef = null;
        });
        return modalRef;
    }
}
