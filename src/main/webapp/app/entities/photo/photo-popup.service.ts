import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { HttpResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { Photo } from './photo.model';
import { PhotoService } from './photo.service';

@Injectable()
export class PhotoPopupService {
    private ngbModalRef: NgbModalRef;

    constructor(
        private datePipe: DatePipe,
        private modalService: NgbModal,
        private router: Router,
        private photoService: PhotoService

    ) {
        this.ngbModalRef = null;
    }

    open(component: Component, id?: number | any, tagSelected?: string): Promise<NgbModalRef> {
        return new Promise<NgbModalRef>((resolve, reject) => {
            const isOpen = this.ngbModalRef !== null;
            if (isOpen) {
                resolve(this.ngbModalRef);
            }

            if (id) {
                this.photoService.find(id)
                    .subscribe((photoResponse: HttpResponse<Photo>) => {
                        const photo: Photo = photoResponse.body;
                        photo.dateCreated = this.datePipe
                            .transform(photo.dateCreated, 'yyyy-MM-ddTHH:mm:ss');
                        this.ngbModalRef = this.photoModalRef(component, photo, tagSelected);
                        resolve(this.ngbModalRef);
                    });
            } else {
                // setTimeout used as a workaround for getting ExpressionChangedAfterItHasBeenCheckedError
                setTimeout(() => {
                    this.ngbModalRef = this.photoModalRef(component, new Photo(), tagSelected);
                    resolve(this.ngbModalRef);
                }, 0);
            }
        });
    }

    photoModalRef(component: Component, photo: Photo, tagSelected?: string): NgbModalRef {
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
