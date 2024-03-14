import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule } from '@angular/forms';
import { APP_BASE_HREF } from '@angular/common';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { TimelineFilterBarChartComponent } from './custom-charts/timeline-filter-bar-chart/timeline-filter-bar-chart.component';
import { NgxChartsModule } from '@swimlane/ngx-charts/ngx-charts.module';
import { NgxUIModule } from '@swimlane/ngx-ui';
import { ComboChartComponent, ComboSeriesVerticalComponent } from './custom-charts/combo-chart';
import { TopViewComponent } from './top-view/top-view.component';
import { RegressionComponent } from './regression/regression.component';

@NgModule({
  providers: [
    {
      provide: APP_BASE_HREF,
      useFactory: getBaseLocation
    }
  ],
  imports: [
    NgxChartsModule,
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    NgxUIModule,
    HttpClientModule,
  ],
  declarations: [
    AppComponent,
    
    TimelineFilterBarChartComponent,
    ComboChartComponent,
    ComboSeriesVerticalComponent,
    TopViewComponent,
    RegressionComponent
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}

export function getBaseLocation() {
  const paths: string[] = location.pathname.split('/').splice(1, 1);
  const basePath: string = (paths && paths[0]) || '';
  return '/' + basePath;
}
