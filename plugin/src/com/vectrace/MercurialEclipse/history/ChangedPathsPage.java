import org.eclipse.jface.text.ITextInputListener;
		for (int lineNo = 0; lineNo < nrOfLines && !monitor.isCanceled();) {
						IRegion lineInformation = document.getLineInformation(lineNo);
						Color lineColor = getDiffLineColor(document.get(offset, length));
			} finally {
			while (display.readAndDispatch()) {
	class UpdateDiffViewerJob extends UIJob implements ITextInputListener {
		private IProgressMonitor monitor;
		public IStatus runInUIThread(IProgressMonitor progressMonitor) {

			this.monitor = progressMonitor;
			try {
				diffTextViewer.addTextInputListener(this);
				applyLineColoringToDiffViewer(monitor);
			} finally {
				diffTextViewer.removeTextInputListener(this);
				this.monitor = null;
			}
			return progressMonitor.isCanceled()? Status.CANCEL_STATUS : Status.OK_STATUS;

		/**
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
			monitor.setCanceled(true);
		}

		/**
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		}