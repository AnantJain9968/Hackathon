const chartGroups = [
  {
    name: 'Bar Charts',
    charts: [
      
      {
        name: 'Stacked Vertical Bar Chart',
        selector: 'bar-vertical-stacked',
        inputFormat: 'multiSeries',
        options: [
          'animations',
          'colorScheme',
          'schemeType',
          'showXAxis',
          'showYAxis',
          'gradient',
          'barPadding',
          'noBarWhenZero',
          'showLegend',
          'legendTitle',
          'legendPosition',
          'showXAxisLabel',
          'xAxisLabel',
          'showYAxisLabel',
          'yAxisLabel',
          'showGridLines',
          'roundDomains',
          'tooltipDisabled',
          'yScaleMax',
          'showDataLabel',
          'trimXAxisTicks',
          'trimYAxisTicks',
          'rotateXAxisTicks',
          'maxXAxisTickLength',
          'maxYAxisTickLength',
          'wrapTicks'
        ],
        defaults: {
          yAxisLabel: 'Total Cases',
          xAxisLabel: 'Week',
          linearScale: true
        }
      },
    ],
  },
  {
    name: 'Line/Area Charts',
    charts: [
      {
        name: 'Line Chart',
        selector: 'line-chart',
        inputFormat: 'multiSeries',
        options: [
          'animations',
          'colorScheme',
          'schemeType',
          'showXAxis',
          'showYAxis',
          'gradient',
          'showLegend',
          'legendTitle',
          'legendPosition',
          'showXAxisLabel',
          'xAxisLabel',
          'showYAxisLabel',
          'yAxisLabel',
          'autoScale',
          'timeline',
          'showGridLines',
          'curve',
          'rangeFillOpacity',
          'roundDomains',
          'tooltipDisabled',
          'showRefLines',
          'referenceLines',
          'showRefLabels',
          'xScaleMin',
          'xScaleMax',
          'yScaleMin',
          'yScaleMax',
          'trimXAxisTicks',
          'trimYAxisTicks',
          'rotateXAxisTicks',
          'maxXAxisTickLength',
          'maxYAxisTickLength',
          'wrapTicks'
        ],
        defaults: {
          yAxisLabel: 'Cases',
          xAxisLabel: 'Week',
          linearScale: true
        }
      },
      
    ]
  },
  
  {
    name: 'Demos',
    charts: [
      {
        name: 'Combo Chart',
        selector: 'combo-chart',
        inputFormat: 'comboChart',
        options: [
          'animations',
          'showXAxis',
          'showYAxis',
          'gradient',
          'showLegend',
          'noBarWhenZero',
          'legendTitle',
          'legendPosition',
          'showXAxisLabel',
          'xAxisLabel',
          'showYAxisLabel',
          'yAxisLabel',
          'showGridLines',
          'roundDomains',
          'tooltipDisabled'
        ]
      },
      
    ]
  }
];

export default chartGroups;
